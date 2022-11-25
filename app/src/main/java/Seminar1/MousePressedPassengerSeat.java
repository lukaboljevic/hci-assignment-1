/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Seminar1;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author Luka
 */
public class MousePressedPassengerSeat extends MouseAdapter {
    
    /**
     * A mouse pressed listener for the PassengerSeat object. We can click the 
     * radio button or the panel itself, but we process those events the same.
     * @param me: MouseEvent object
     */
    @Override
    public void mousePressed(MouseEvent me){
        PassengerSeat origin; // the PassengerSeat object where this click originated
        
        try {
            // The panel was clicked
            origin = (PassengerSeat)me.getComponent();
        } catch (ClassCastException ex){
            // Or the button was clicked
            JRadioButton b = (JRadioButton)me.getComponent(); // the radio button itself
            origin = (PassengerSeat)b.getParent(); // actual PassengerSeat object
        }
        
        int originPassengerNumber = origin.getPassengerNumber(); // get passenger number associated with this seat
        if (origin.getSelecting()){
            // the button is selected, and we clicked here, so return
            return;
        }
        
        // Get the parent of the origin component (seatPanelPsgs, in the MainFrame)
        JPanel seatPanel = (JPanel)origin.getParent();
        
        for (int i = 0; i < seatPanel.getComponentCount(); i ++){ // walk through all PassengerSeat panels
            PassengerSeat curr;
            try {
                curr = (PassengerSeat)seatPanel.getComponent(i);
            } catch (ClassCastException ex){
                // we encountered a vertical box, so just continue
                continue;
            }
            int currPassengerNumber = curr.getPassengerNumber();
            
            // If we're talking about the same component, skip
            if (originPassengerNumber == currPassengerNumber){
                continue;
            }
            
            // If it's a different component, then deselect the button from that one,
            // and select the button in the component where the click originated
            if (curr.getSelecting()){
                curr.setSelecting(false);
                origin.setSelecting(true);
                return;
            }
        }
        
        // If we made it here, it means that no buttons were selected, so
        // just set this one to be clicked
        origin.setSelecting(true);
    }
    
}
