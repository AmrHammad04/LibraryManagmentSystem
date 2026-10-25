package com.library.demo.controller;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
public class FormBindingAdvice {

    @InitBinder
    public void trimFormText(WebDataBinder binder) {
        binder.registerCustomEditor(
                String.class,
                new StringTrimmerEditor(true));
    }
}