package com.itextpdf.svg.renderers.impl;

import com.itextpdf.svg.renderers.ISvgNodeRenderer;
import com.itextpdf.svg.renderers.SvgDrawContext;

import java.util.ArrayList;
import java.util.List;

/**
 * A dummy implementation of {@link ISvgNodeRenderer} for testing purposes
 */
public class DummySvgNodeRenderer implements ISvgNodeRenderer {
    ISvgNodeRenderer parent;
    List<ISvgNodeRenderer> children;
    String name;

    public DummySvgNodeRenderer(String name, ISvgNodeRenderer parent){
        this.name = name;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    @Override
    public ISvgNodeRenderer getParent() {
        return parent;
    }

    @Override
    public void draw(SvgDrawContext context) {
        System.out.println(name+": Drawing in dummy node, children left: " + children.size());
    }

    @Override
    public void addChild(ISvgNodeRenderer child) {
        children.add(child);
    }

    @Override
    public List<ISvgNodeRenderer> getChildren() {
        return children;
    }

    @Override
    public String toString(){
        return name;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof DummySvgNodeRenderer)){
            return false;
        }
        //Name
        DummySvgNodeRenderer otherDummy = (DummySvgNodeRenderer)o;
        if(!this.name.equals(otherDummy.name)){
            return false;
        }
        //children
        if(!(this.children.isEmpty() && otherDummy.children.isEmpty())){
            if(this.children.size() != otherDummy.children.size()){
                return false;
            }
            boolean iterationResult = true;
            for (int i = 0; i < this.children.size(); i++) {
                iterationResult &= this.children.get(i).equals(otherDummy.getChildren().get(i));
            }
            if(!iterationResult){
                return false;
            }
        }
        return true;
    }

}
