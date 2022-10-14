package org.javers.core.cases

import org.apache.commons.lang3.ObjectUtils
import org.javers.core.JaversBuilder
import org.javers.core.metamodel.annotation.Entity
import org.javers.core.metamodel.annotation.Id
import org.javers.repository.jql.QueryBuilder
import org.jetbrains.annotations.NotNull
import org.junit.Test
import spock.lang.Specification

class Case1219SortedSetError extends Specification {

    @Entity
    class ContainerWithSortedSet {

        @Id
        Long id
        SortedSet<Some> set

    }

    @Entity
    class ContainerWithRegularSet {

        @Id
        Long id
        Set<Some> set

    }

    class Some implements Comparable<Some> {

        String id

        Some(String id) {
            this.id = id
        }

        @Override
        int compareTo(@NotNull Some that) {
            return ObjectUtils.compare(this.id, that.id)
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            Some some = (Some) o

            if (id != some.id) return false

            return true
        }

        int hashCode() {
            return (id != null ? id.hashCode() : 0)
        }

    }

    @Test
    void "when using sorted set in an entity, and getting its shadows, no exception is thrown, and shadow is present"() {

        given:
        var javers = JaversBuilder.javers().build()

        var sortedSet = new TreeSet()
        sortedSet.add(new Some("z88S"))
        var container = new ContainerWithSortedSet()
        container.id = 1L
        container.set = sortedSet
        javers.commit("some", container)

        when:
        var query = QueryBuilder.byInstanceId(container.id, ContainerWithSortedSet.class).limit(1).build()

        then:
        var shadow = javers.findShadowsAndStream(query).findFirst()
        shadow.isPresent()
    }

    @Test
    void "when using regular set in an entity, and getting its shadows, no exception is thrown, and shadow is present"() {

        given:
        var javers = JaversBuilder.javers().build()

        var regularSet = new HashSet()
        regularSet.add(new Some("0c6W8i"))
        var container = new ContainerWithRegularSet()
        container.id = 2L
        container.set = regularSet
        javers.commit("some", container)

        when:
        var query = QueryBuilder.byInstanceId(container.id, ContainerWithRegularSet.class).limit(1).build()

        then:
        var shadow = javers.findShadowsAndStream(query).findFirst()
        shadow.isPresent()
    }

}
