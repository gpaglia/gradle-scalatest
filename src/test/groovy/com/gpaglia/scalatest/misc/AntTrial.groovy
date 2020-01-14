package com.gpaglia.scalatest.misc

import org.apache.tools.ant.types.Path
import org.junit.jupiter.api.Test

class AntTrial {
    private static final String CP = System.getProperty("java.class.path")
    private static final String[] CP_ARRAY = CP.split(';')
    private static final String CP_DIR = 'D:/DiskZ_new/Private-Project-Folder/gradle-scalatest/build/classes/groovy/test'

    @Test
    public void test1() {
        final ant = new AntBuilder()

        final Path path = ant.path(id: 'cp')
        for (String p: CP_ARRAY) {
            path.add(
                ant.path(location: p)
            )
        }

        ant.echo('Hello my dear...')
        // ant.echo('path is ' + path.toList().toString())
        ant.echo('Class: ' + AntTask.class.toString() + ', ' + 'com.gpaglia.scalatest.misc.AntTask')

        ant.taskdef(
            name: 'hiho',
            classname: AntTask.class.getName(),
            classpath: path
        )

        /*
            alternative:

            classpath: {
                ant.fileset(
                    dir: CP_DIR,
                    include: <glob>
                )
            }
        */

        ant.hiho()
    }
}
