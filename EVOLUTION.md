Move to ant task integration:
`task scalaTest(dependsOn: testClasses) << { 
    ant.taskdef(name: 'scalatest', 
                classname: 'org.scalatest.tools.ScalaTestAntTask', 
                classpath: sourceSets.test.runtimeClasspath.asPath 
    ) 
    ant.scalatest(runpath: sourceSets.test.compileClasspath, 
                fork: 'false', 
                haltonfailure: 'true', 
                suite: 'TranslatorTest') 
                { reporter(type: 'stdout') }
 } `