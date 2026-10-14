package com.airtortilla.algoritmos;

import com.airtortilla.model.Reserva;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OrdenadorMergeSort {

    public static List<Reserva> ordenar(List<Reserva> lista) {
        if (lista == null || lista.size() <= 1) {
            return lista == null ? new ArrayList<>() : new ArrayList<>(lista);
        }
        return mergeSort(new ArrayList<>(lista), Comparator.naturalOrder());
    }

    public static List<Reserva> ordenar(List<Reserva> lista, Comparator<Reserva> comparador) {
        if (lista == null || lista.size() <= 1) {
            return lista == null ? new ArrayList<>() : new ArrayList<>(lista);
        }
        return mergeSort(new ArrayList<>(lista), comparador != null ? comparador : Comparator.naturalOrder());
    }

    private static List<Reserva> mergeSort(List<Reserva> lista, Comparator<Reserva> comparador) {
        if (lista.size() <= 1) {
            return lista;
        }

        int medio = lista.size() / 2;
        List<Reserva> izquierda = new ArrayList<>(lista.subList(0, medio));
        List<Reserva> derecha = new ArrayList<>(lista.subList(medio, lista.size()));

        izquierda = mergeSort(izquierda, comparador);
        derecha = mergeSort(derecha, comparador);

        return fusionar(izquierda, derecha, comparador);
    }

    private static List<Reserva> fusionar(List<Reserva> izquierda, List<Reserva> derecha, Comparator<Reserva> comparador) {
        List<Reserva> resultado = new ArrayList<>(izquierda.size() + derecha.size());
        int i = 0;
        int j = 0;

        while (i < izquierda.size() && j < derecha.size()) {
            Reserva elemIzq = izquierda.get(i);
            Reserva elemDer = derecha.get(j);

            if (comparador.compare(elemIzq, elemDer) <= 0) {
                resultado.add(elemIzq);
                i++;
            } else {
                resultado.add(elemDer);
                j++;
            }
        }

        while (i < izquierda.size()) {
            resultado.add(izquierda.get(i));
            i++;
        }

        while (j < derecha.size()) {
            resultado.add(derecha.get(j));
            j++;
        }

        return resultado;
    }
}
