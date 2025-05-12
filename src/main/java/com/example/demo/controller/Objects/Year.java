package com.example.demo.controller.Objects;

import java.util.*;

public class Year {
    public int currentYear;
    public List<Integer> years;
    public Year()
    {
        years = new ArrayList<Integer>();

        currentYear = java.time.Year.now().getValue();

        years.add(currentYear - 2);
        years.add(currentYear - 1);
        years.add(currentYear);
        years.add(currentYear + 1);
        years.add(currentYear + 2);
        
        

    }

    public void setCurrentYear(int currentYear)
    {
        this.currentYear = currentYear;
    }

    public int getCurrentYear()
    {
        return currentYear;
    }

    public void setYears(List<Integer> years)
    {
        this.years = years;
    }

    public List<Integer> getYears()
    {
        return years;
    }
}
