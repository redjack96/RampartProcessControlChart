package com.giacomolorenzo.rossi.data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Classe che definisce un tipo di dato che è possibile scrivere su un file CSV.
 * L' ho resa astratta per evitare che sia istanziata per sbaglio.
 */
public abstract class Writable implements ReadableWritable{

    protected final String getValue(Field f, Writable writable) {
        var s = "";
        try {
            Object value = f.get(writable);
            if (value instanceof Date) {
                s = new SimpleDateFormat("yyyy-MM-dd").format(value);
            } else if (value instanceof Double) {
                var d = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                s = d.format(value);
            } else {
                s = String.valueOf(value);
            }
        } catch (IllegalAccessException e) {
            Logger.getLogger(Writable.class.getName()).warning(e.getMessage());
        }
        return s;
    }


    /**
     * @return Data una istanza di Writable, restituisce un array di stringhe
     * che rappresenta i valori della classe da scrivere in un CSV con SimpleCSV
     */
    public String[] getValues() {
        var array = new String[accessibleFields()];
        var i = 0;
        for (Field f : this.getClass().getDeclaredFields()) {
            /* Prima devo essere sicuro che non è una costante e poi posso accedere al campo. Ai campi statici non possono essere passate istanze*/
            if (isAccessible(f)) {
                array[i++] = getValue(f, this);
            }
        }
        return array;
    }

    /**
     * Metodo che restituisce l' header del csv a partire dall' istanza corrente di Writable.
     * Sovrascritto (Override) solo da SimpleCommit
     *
     * @return Restituisce l' header del csv per l' istanza corrente di Writable
     */
    protected String[] getHeader() {
        var fields = getClass().getDeclaredFields();
        var array = new String[accessibleFields()];
        var i = 0;
        for (Field f : fields) {
            /* Devo prima controllare se è costante, altrimenti non posso passargli l' istanza perché i campi statici non possono essere acceduti da essa  */
            if (isAccessible(f)) {
                array[i++] = f.getName();
            }
        }
        return array;
    }

    /**
     * Metodo statico per ottenere l' header del CSV
     * @param <T> oggetto del tipo della classe.
     * @param clazz tipo della classe.
     * @param project il progetto a cui si riferisce il Writable
     * @return Array di stringhe che rappresenta l' header del CSV, ricavato dai campi package private.
     */
    public static <T extends Writable> String[] getCSVHeader(Class<T> clazz, Project project) {
        try {
            return clazz.getDeclaredConstructor(Project.class).newInstance(project).getHeader();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return new String[]{};
    }

    private int accessibleFields() {
        Field[] declaredFields = this.getClass().getDeclaredFields();
        long count = Arrays.stream(declaredFields).filter(field -> !isAccessible(field)).count();
        return (int) (declaredFields.length - count);
    }

    private boolean isAccessible(Field f) {
        return !isConstant(f)
                && f.canAccess(this);
    }

    private boolean isConstant(Field f) {
        int modifiers = f.getModifiers();
        return Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
    }
}
