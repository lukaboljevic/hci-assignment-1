/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Seminar1;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 *
 * @author Luka
 */
public class ActionPerformedSeatButton implements ActionListener {
    
    /**
     * An action performed action listener, for the JToggleButtons used for seat selection.
     * @param e: ActionEvent object
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        JToggleButton origin = (JToggleButton)e.getSource();
        if (origin.getForeground() == Color.BLACK){
            // this means that this button was previously selected by 
            // a passenger, so just return (and select the button again
            // because it was unselected now).
            origin.setSelected(true);
            return;
        }
        
        Component seatTable = origin.getParent(); // the seat table containing all seat buttons
        
        JPanel containerPanel = (JPanel)seatTable.getParent(); // container panel, with seatPanelSeatTable and seatPanelPsgs panels
        
        JPanel seatPanelPsgs = (JPanel)containerPanel.getComponent(1); // the panel containing all PassengerSeat objects,
        // corresponding to all passengers
        
        // We want to find the PassengerSeat object, whose radio button is
        // pressed (meaning this passenger is selecting a seat), and then
        // change the origin togglebutton's text to contain that passenger's
        // initials - meaning he/she selected a seat.
        for (int i = 0; i < seatPanelPsgs.getComponentCount(); i ++){
            PassengerSeat ps;
            try {
                ps = (PassengerSeat)seatPanelPsgs.getComponent(i);
            } catch (ClassCastException ex){
                // we've encountered a box, so skip
                continue;
            }
            
            if (ps.getSelecting()){
                if (!(ps.getSeat().equals(""))){
                    JToggleButton prevSeatButton = ps.getButtonSeat();
                    prevSeatButton.setForeground(Color.WHITE);
                    prevSeatButton.setText("AA");
                    prevSeatButton.setSelected(false);
                    
                    ps.setSeat("");
                }
                
                String nameInitial = ps.getPsgName().substring(0, 1);
                String surnameInitial = ps.getSurname().substring(0, 1);
                origin.setForeground(Color.BLACK);
                origin.setText(nameInitial + surnameInitial);
                // ofc, origin button is now selected
                
                ps.setButtonSeat(origin);
                ps.setSeat(origin.getName());
                ps.setLabelSeatText("Seat: " + ps.getSeat());
                
                return;
            }
        }
        
        // Getting here means that no passenger was selecting a seat, so just
        // unselect the button
        origin.setForeground(Color.WHITE);
        origin.setText("AA");
        origin.setSelected(false);
    }
    
}
