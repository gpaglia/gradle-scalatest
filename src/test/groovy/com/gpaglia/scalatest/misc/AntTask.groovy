package com.gpaglia.scalatest.misc

import org.apache.tools.ant.Task

class AntTask extends Task {
    public void execute() {
        final String message = 'Hello, this is my message!'

        System.out.println('Message is: ' + message)
        System.out.println('Colored message is: \u001b[32m' + message + '\u001b[0m')
    }
}
