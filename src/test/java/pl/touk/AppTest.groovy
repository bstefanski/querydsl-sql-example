package pl.touk

import com.querydsl.sql.Configuration
import com.querydsl.sql.H2Templates
import com.querydsl.sql.SQLQueryFactory
import com.querydsl.sql.SQLTemplates
import spock.lang.Specification

import javax.inject.Provider
import java.sql.Connection
import java.sql.DriverManager

class AppTest extends Specification {

    SQLQueryFactory queryFactory

    void setup() {
        SQLTemplates templates = new H2Templates()
        Configuration configuration = new Configuration(templates)
        queryFactory = new SQLQueryFactory(configuration, provider)
    }

    def "should execute query"() {
        given:
            QPerson qPerson = QPerson.person
        when:
            String lastName = queryFactory.select(qPerson.lastName)
                    .from(qPerson)
                    .where(qPerson.firstName.eq('Jon'))
                    .fetchFirst()
        then:
            lastName == 'Doe'
        when:
            Person person = queryFactory
                    .select(qPerson)
                    .from(qPerson)
                    .where(qPerson.firstName.eq('Jon'))
                    .fetchOne()
        then:
            person.lastName == 'Doe'
            person.age == 20
    }

    def "should execute dml statements"() {
        given:
            QPerson qPerson = new QPerson("qPerson")
        when: "insert"
            Person p = new Person(firstName: 'a', lastName: 'b', age: 18)
            Integer id = queryFactory
                    .insert(qPerson)
                    .populate(p)
                    .executeWithKey(qPerson.id)
            p.id = id
        then:
            p.id != null

        when: "update"
            p.lastName = "c"
            queryFactory
                    .update(qPerson)
                    .populate(p)
                    .where(qPerson.id.eq(p.id))
                    .execute()
        then:
            queryFactory.select(qPerson.lastName).from(qPerson).where(qPerson.firstName.eq('a')).fetchFirst() == 'c'

        when: 'delete'
            queryFactory
                    .delete(qPerson)
                    .where(qPerson.id.eq(p.id)).execute()
        then:
            queryFactory.from(qPerson).fetchCount() == 1
    }

    Provider<Connection> provider = new Provider<Connection>() {
        @Override
        public Connection get() {
            Class.forName("org.h2.Driver")
            return DriverManager.getConnection("jdbc:h2:file:~/test", "sa", "")
        }
    }
}
