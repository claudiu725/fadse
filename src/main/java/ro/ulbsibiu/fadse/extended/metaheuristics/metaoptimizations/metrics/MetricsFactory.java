/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.metaheuristics.metaoptimizations.metrics;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Cristian
 */
public class MetricsFactory {
    private static MetricsFactory instance;
    
    private MetricsFactory() { }
    
    public Metric createMetrics(String name) {
        try {
            String className = name + "Metric";
            className = MetricsFactory.class.getPackage().getName() + "." + className;
            Class metricClass = Class.forName(className);
            Constructor defaultConstructor = metricClass.getConstructor();
            Metric metric = (Metric)defaultConstructor.newInstance();
            return metric;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | 
                IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(MetricsFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static MetricsFactory getInstance() {
        if (instance == null)
            instance = new MetricsFactory();
        return instance;
    }
}
