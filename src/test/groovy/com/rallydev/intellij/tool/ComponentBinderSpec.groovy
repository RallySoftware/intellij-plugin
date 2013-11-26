package com.rallydev.intellij.tool

import spock.lang.Specification

import javax.swing.JComboBox
import javax.swing.JLabel

class ComponentBinderSpec extends Specification {

    def 'binding component to label sets bean value on property change'() {
        SomeBean bean = new SomeBean(prop: 'initialValue')
        ComponentBinder binder = new ComponentBinder(bean)
        JLabel label = new JLabel()

        expect:
        binder.bind(label, 'prop')

        when:
        label.setText('newValue')

        then:
        bean.prop == 'newValue'
    }

    def 'binding component to comboBox sets bean value on property change'() {
        SomeBean bean = new SomeBean(prop: 'initialValue')
        ComponentBinder binder = new ComponentBinder(bean)
        JComboBox comboBox = new JComboBox(['0', '1', '2', '3'] as Vector)

        expect:
        binder.bind(comboBox, 'prop')

        when:
        comboBox.setSelectedIndex(2)

        then:
        bean.prop == '2'
    }

    def 'binding component to textPane sets bean value on property change'() {
        SomeBean bean = new SomeBean(prop: 'initialValue')
        ComponentBinder binder = new ComponentBinder(bean)
        CustomTextPane textPane = new CustomTextPane()

        expect:
        binder.bind(textPane, 'prop')

        when:
        textPane.setText('Some new content')

        then:
        bean.prop == 'Some new content'
    }

}

class SomeBean {
    String prop
}
