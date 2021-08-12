package com.giacomolorenzo.rossi.data;

import java.lang.reflect.InvocationTargetException;

/**
 * Marca una classe che sia o Readable o Writable.
 */
public interface ReadableWritable {
    String getFileNameSuffix();

    /**
     * Ricava il nome del file csv dato il progetto e il tipo di dato da salvare.
     * È necessario che la classe T abbia un costruttore che accetta un parametro Project.
     * @param project il progetto relativo al file.
     * @param clazz Il tipo di dato di una classe Writable o Readable
     * @param <T> una classe di tipo Writable o Readable
     * @return il nome completo del file csv.
     */
    static <T extends ReadableWritable> String getCsvName(Project project, Class<T> clazz){
        try {
            var classe = clazz.getDeclaredConstructor(Project.class).newInstance(project);
            return project + classe.getFileNameSuffix();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("controlla che il tipo di dato sia corretto.");
        }
    }
}
