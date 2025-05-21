package com.example.demo.controller.Objects;

import java.util.*;

public class Month {
    public List<String> months;

    public Month()
    {
        months = new ArrayList<String>();

        months.add("Janeiro");
        months.add("Fevereiro");
        months.add("Marco");
        months.add("Abril");
        months.add("Maio");
        months.add("Junho");
        months.add("Julho");
        months.add("Agosto");
        months.add("Setembro");
        months.add("Outubro");
        months.add("Novembro");
        months.add("Dezembro");
    }

    public List<String> getMonths()
    {
        return months;
    }

    public void setMonths(List<String> months)
    {
        this.months = months;
    }
}
