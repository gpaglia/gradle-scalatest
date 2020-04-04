package com.gpaglia.gradle.scalatest.misc

import org.junit.jupiter.api.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is

class GroovyNamedArgsTest {

    @Test
    public void test1() {
        meth(key1: 'Gianni', key2: 'Patrizia') {
            System.out.println('In closure...')
            doit() {
                System.out.println('Doing it!!')
            }
        }
    }

    @Test
    public void test2() {
        meth([key1: 'Gianni', key2: 'Patrizia']) {
            System.out.println('In closure...')
            doit() {
                System.out.println('Doing it!!')
            }
        }
    }

    @Test
    public void test3() {
        def f = { String s -> s.endsWith('X') }

        assertThat(f('AAA'), is(false))
        assertThat(f('AAX'), is(true))
    }

    private meth(Map<String, ?> map, Closure c) {
        map.each {entry -> System.out.println("Key: ${entry.key}, Value: ${entry.value}")}
        c.call()
    }

    private void doit(Closure c) {
        c.call()
    }
}
