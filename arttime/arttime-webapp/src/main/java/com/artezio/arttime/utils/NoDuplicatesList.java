package com.artezio.arttime.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
//TODO replace with set
public class NoDuplicatesList<E> extends ArrayList<E> {

    private static final long serialVersionUID = -9186366402131160419L;

    public NoDuplicatesList() { }

    public NoDuplicatesList(Collection<? extends E> c) {
        this.addAll(c);
    }

    public boolean add(E e) {
        return contains(e) ? false : super.add(e);
    }

    public void add(int index, E e) {
        if (!contains(e)) {
            super.add(index, e);
        }
    }

    public boolean addAll(Collection<? extends E> c) {
        Collection<E> itemsToAdd = new LinkedHashSet<E>(c);
        itemsToAdd.removeAll(this);
        return super.addAll(itemsToAdd);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        Collection<E> itemsToAdd = new LinkedHashSet<E>(c);
        itemsToAdd.removeAll(this);
        return super.addAll(index, itemsToAdd);
    }

}
