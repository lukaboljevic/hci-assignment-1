/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Seminar1;

import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Luka
 */
public final class Utils {
    /*
    Class with utility functions, that do not depend 
    on private variables from the JFrame. This is done
    to avoid overcluttering the code in MainFrame.java.
    */
    
    public static final double BUSINESS_CLASS = 1.5; // business class is 50% more expensive
    
    public static final double MORNING = 0.9; // morning flight is 10% cheaper
    public static final double AFTERNOON = 1.0;
    public static final double EVENING = 1.1; // evening flight is 10% more expensive
    
    public static final int ADULT_PRICE = 85; // ticket price for an adult, with meal price and regular luggage included
    public static final int CHILD_PRICE = 50; // ticket price for a child, with meal price and regular luggage included
    
    public static final int LUGGAGE_EXTRA = 25; // extra luggage is an additional 25 euros, regardless of class
    
    public static final String EURO = "€";
    
    
    public static void showErrorMessage(JFrame frame, String message){
        /**
         * Show a JOptionPane error message.
         */
        JOptionPane.showMessageDialog(frame, message, 
                "Error with input data",JOptionPane.ERROR_MESSAGE);
    }
    
    public static void showOrderConfirmedInfoMessage(JFrame frame, String message){
        /**
         * Show a JOptionPane information message.
         */
        JOptionPane.showMessageDialog(frame, message, 
                "Order confirmed", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static int showConfirmationMessage(JFrame frame, Object object, String title, boolean icon){
        /**
         * Show a JOptionPane confirmation message. If we want to show the icon,
         * this means we want to confirm clearing entered information on a panel.
         * If we don't want to show the icon, we are showing full booking information,
         * which is a plain message.
         */
        
        int msgType = icon ? JOptionPane.QUESTION_MESSAGE : JOptionPane.PLAIN_MESSAGE;
        return JOptionPane.showConfirmDialog(frame, object, title,
                JOptionPane.YES_NO_CANCEL_OPTION, msgType);
    }
    
    public static String parseInvalidDateMessage(String error){
        /**
         * Parse the error message from DateTimeException, to extract just the date and month.
         * Used only for printing purposes.
         */
        
        // Example error: "Invalid date 'FEBRUARY 31'"
        String[] parts = error.split(" ");
        String month = parts[2].substring(1).toLowerCase();
        String day = parts[3].substring(0, parts[3].length() - 1);
        return month.substring(0, 1).toUpperCase() + month.substring(1) + " " + day;
    }
    
    public static boolean buttonFromGroupSelected(ButtonGroup group){
        /**
         * Return true if any button from this button group is selected.
         */
        Enumeration<AbstractButton> buttonsTime = group.getElements();
        while (buttonsTime.hasMoreElements()){
            AbstractButton b = buttonsTime.nextElement();
            if (b.isSelected()){
                return true;
            }
        }
        return false;
    }
    
    public static String getSelectedRadioButtonName(ButtonGroup group){
        /**
         * Return the name of the currently selected radio button from a button group.
         */
        Enumeration<AbstractButton> buttonsTime = group.getElements();
        while (buttonsTime.hasMoreElements()){
            AbstractButton b = buttonsTime.nextElement();
            if (b.isSelected()){
                return b.getName();
            }
        }
        return "";
    }
    
}
