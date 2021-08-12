package com.giacomolorenzo.rossi.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public interface Readable extends ReadableWritable{

    /**
     * Questo metodo imposta tutti i campi non-private della classe
     * @return readable
     */
    Readable loadFromCSV(String[] csvRow);

    void setProject(Project project);

    @SuppressWarnings("unchecked")
    static <T extends Readable> T staticLoadFromCsv(String[] csvRow, Class<T> clazz, Project project){
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(Project.class);
            return (T) constructor.newInstance(project).loadFromCSV(csvRow);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
