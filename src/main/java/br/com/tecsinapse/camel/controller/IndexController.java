package br.com.tecsinapse.camel.controller;

import com.ocpsoft.pretty.faces.annotation.URLMapping;

import javax.faces.bean.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
@URLMapping(id = "index", pattern = "/index/", viewId = "/jsf/index.xhtml")
public class IndexController implements Serializable {

	private static final long serialVersionUID = 1L;


}
