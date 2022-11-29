/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Seminar1;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Luka
 */

public class MainFrame extends javax.swing.JFrame {

    private class ItemListenerFlightType implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED){
                String type = Utils.getSelectedRadioButtonName(groupFlightType);
                boolean returnDateVisible = type.equals("Return");
                
                labelReturnDate.setEnabled(returnDateVisible);
                cboxReturnDay.setEnabled(returnDateVisible);
                cboxReturnMonth.setEnabled(returnDateVisible);
                cboxReturnYear.setEnabled(returnDateVisible);
            }
        }
    }
    
    private class MousePressedSeatButton extends MouseAdapter {
        
        // has to be mousePressed, mouseClicked doesn't give us what we want.
        @Override
        public void mousePressed(MouseEvent me){
            // The JToggleButton has an invisible -1-1 written inside of it, which
            // is there so the size of the buttons remains the same at all times.
            // Setting the foreground to this color makes sure it is not seen
            // for that split second when the button is pressed. Also works when
            // "unselecting" this button, since we are also setting the foreground
            // color in ActionPerformedSeatButton class.
            ((JToggleButton)me.getComponent()).setForeground(App.BUTTON_CLICKED_ON_COLOR);
        }
    }
    
    
    private double totalBookingPrice;
    private int numAdultsSelected = -1; // stores the previously selected amount of adult passengers; -1 when no search has yet been made
    private int numChildrenSelected = -1; // same as above, just for amount of child passengers
    private final ArrayList<PassengerInfo> passengerList = new ArrayList<>(); // all passengers
    private final Map<Integer, String> passengerSeat = new HashMap<>(); // passenger number to seat map
    
    
    /**
     * Various helper methods
     */
    
    private String buildBookingDetailsMessage(){
        /**
         * Build the message that shows all booking details.
         */
        String bookingMessage = "<html><center><b><span style=\"font-size:18px\">Booking details</span></b></center><br>";
        bookingMessage += "<b>" + Utils.getSelectedRadioButtonName(this.groupFlightType) + "</b>"
                + " flight from <b>" + this.cboxFrom.getSelectedItem()
                + "</b> to <b>" + this.cboxTo.getSelectedItem() + "</b><br><br>"
                + "Outbound date: <b>" + this.cboxOutboundMonth.getSelectedItem() + " "
                + this.cboxOutboundDay.getSelectedItem() + ", " + this.cboxOutboundYear.getSelectedItem() + "</b><br>";
        
        if (Utils.getSelectedRadioButtonName(this.groupFlightType).equals("Return")){
            bookingMessage += "Return date: <b>" + this.cboxReturnMonth.getSelectedItem() + " "
                + this.cboxReturnDay.getSelectedItem() + ", " + this.cboxReturnYear.getSelectedItem() + "</b><br>";
        }
        bookingMessage += "Flight time: <b>" + Utils.getSelectedRadioButtonName(this.groupFlightTime) + "</b><br>"
                + "Flight class: <b>" + Utils.getSelectedRadioButtonName(this.groupFlightClass) + "</b><br>"
                + "Number of passengers: <b>" + (this.numAdultsSelected + this.numChildrenSelected) + "</b><br><br>";

        for (PassengerInfo pd: this.passengerList){
            bookingMessage += pd.toHTMLString();
            bookingMessage += "Seat: " + this.passengerSeat.get(pd.getPassengerNumber()) + "<br><br>";
        }

        bookingMessage += "Total booking price: <b>" + String.format("%.2f", this.totalBookingPrice)
                + Utils.EURO + "</b><br><br>";
        bookingMessage += "Do you want to confirm your booking?<br><html>";
        
        return bookingMessage;
    }
    
    private boolean proceedWithBooking(){
        /**
         * Show a JOptionPane, which asks us whether we want to proceed
         * i.e. finish with the current booking.
         */
        String bookingMessage = this.buildBookingDetailsMessage();
        JEditorPane ep = new JEditorPane("text/html", ""); // cuz it allows easy html messages
        ep.setEditable(false);
        ep.setFont(App.FONT);
        ep.setText(bookingMessage);
        ep.setCaretPosition(0); // "scroll" back up to the top
        
        // We'll be using a scroll pane because we don't want the option pane to have too big a height
        JScrollPane pane = new JScrollPane(ep);
        pane.setPreferredSize(new Dimension(400, 430));
        pane.setBorder(null);
        
        int proceed = Utils.showConfirmationMessage(this, pane, "Confirm booking", false);
        return !(proceed == JOptionPane.CANCEL_OPTION || proceed == JOptionPane.NO_OPTION
                || proceed == JOptionPane.CLOSED_OPTION);
    }
    
    
    private int currentVisiblePanelIndex(JPanel parent){
        /**
         * For a panel with a Card Layout, return the current visible panel index.
         */
        int num = parent.getComponentCount();
        for (int i = 0; i < num; i ++){
            JPanel p = (JPanel)parent.getComponent(i);
            if (p.isVisible()){
                return i;
            }
        }
        return -1000;
    }
    
    
    private void changePanelTitle(int currentPanelIdx){
        /**
         * Based on the current/next panel we are switching to, change
         * the panel title accordingly.
         */
        switch (currentPanelIdx){
            case 0 -> { // main
                this.labelPanelTitle.setText("Flight search");
            }
            case 1 -> { // passenger
                this.labelPanelTitle.setText("Passenger details");
            }
            case 2 -> { // seat
                this.labelPanelTitle.setText("Seat selection");
            }
            case 3 -> { // payment
                this.labelPanelTitle.setText("Payment");
            }
        }
    }
    
    
    private void updatePassengersList(){
        /**
         * When going from the last subpanel, in the base passenger panel, 
         * update the passengers list to account for any and all changes
         * made while entering passenger information.
         */
        int numSubpanels = this.basePanelPsg.getComponentCount(); // number of subpanels
        for (int i = 0; i < numSubpanels; i ++){
            // Subpanel has either 2 components - they're both of type PassengerInfo, or one of those + a Box
            JPanel p = (JPanel)this.basePanelPsg.getComponent(i);
            
            for (int j = 0; j < p.getComponentCount(); j ++){
                PassengerInfo pd;
                try {
                    pd = (PassengerInfo)p.getComponent(j); // retrieve a single PassengerInfo subpanel
                }
                catch(ClassCastException e){
                    // this is the vertical box/strut, so do nothing.
                    continue;
                }
                // Retrieve the PassengerSeat object corresponding to this passenger.
                // Because of the way we added these objects to seatPanelPsgs in setupSeatPanel, 
                // it infact is this easy to retrieve that corresponding object.
                PassengerSeat ps = (PassengerSeat)this.seatPanelPsgs.getComponent(i*2 + j);
                ps.setPassenger(pd.getPassengerNumber(), pd.getPsgName(), pd.getSurname()); // update!
                
                if (ps.getButtonSeat() != null){
                    // to account for the possibility that someone has updated their 
                    // name/surname for whatever reason, so we change the initials 
                    // on the ToggleButton corresponding to the seat selected by
                    // this passenger
                    String nameInitial = ps.getPsgName().substring(0, 1);
                    String surnameInitial = ps.getSurname().substring(0, 1);
                    ps.getButtonSeat().setText(nameInitial + surnameInitial);
                }
                this.passengerList.set(i*2 + j, pd); // update passenger in the passengers list
            }
        }
    }
    
    
    private void fillCopyFromComboBox(){
        /**
         * Fill up the combo box which is used to fill up payer information 
         * from an existing passenger. The combo box is found on the payment panel.
         */
        int previousSelected = this.cboxCopyFrom.getSelectedIndex(); // 0 or an actual passenger
        this.cboxCopyFrom.removeAllItems();
        this.cboxCopyFrom.insertItemAt("", 0);

        // Insert passenger full names into the list
        for (PassengerInfo pd: this.passengerList){
            String psgName = pd.getPsgName();
            String psgSurname = pd.getSurname();
            String fullName = psgName + " " + psgSurname;
            this.cboxCopyFrom.insertItemAt(fullName, pd.getPassengerNumber());
        }
        this.cboxCopyFrom.setSelectedIndex(previousSelected); // invokes the ItemListener
    }
    
    
    private void fillUpPassengerSeatMap(){
        /**
         * Fill up the passenger to seat (hash)map.
         */
        for (int i = 0; i < this.seatPanelPsgs.getComponentCount(); i ++){
            PassengerSeat ps;
            try {
                ps = (PassengerSeat)this.seatPanelPsgs.getComponent(i);
            }
            catch (ClassCastException ex){
                // vertical box, so continue
                continue;
            }
            this.passengerSeat.put(ps.getPassengerNumber(), ps.getSeat());
        }
    }
    
    
    private void calculateInitialPassengerPrice(){
        /**
         * Calculate the initial flight price for each passenger.
         */
        int numAdults = this.numAdultsSelected;
        int numChildren = this.numChildrenSelected;
        
        // Initial price for each passenger starts at the base price
        double startAdult = Utils.ADULT_PRICE;
        double startChild = Utils.CHILD_PRICE;
        
        // Check what time the flight is
        switch(Utils.getSelectedRadioButtonName(this.groupFlightTime)){
            case "morning" -> {
                startAdult *= Utils.MORNING;
                startChild *= Utils.MORNING;
            }
            case "afternoon" -> {
                startAdult *= Utils.AFTERNOON;
                startChild *= Utils.AFTERNOON;
            }
            case "evening" -> {
                startAdult *= Utils.EVENING;
                startChild *= Utils.EVENING;
            }
        }
        
        // Check if the selected class was business
        if (Utils.getSelectedRadioButtonName(this.groupFlightClass).equals("business")){
            startAdult *= Utils.BUSINESS_CLASS;
            startChild *= Utils.BUSINESS_CLASS;
        }
        
        // If it's a return flight, price is doubled
        if (Utils.getSelectedRadioButtonName(this.groupFlightType).equals("Return")){
            startAdult *= 2;
            startChild *= 2;
        }
        
        // First set the prices for the children, and then for the adults
        // Why? No reason. Why not?
        for (PassengerInfo pd: this.passengerList){
            double extra = 0;
            if (pd.getLuggageExtraSelected()){
                extra = Utils.LUGGAGE_EXTRA;
            }
            if (numChildren > 0){
                pd.setPrice(startChild + extra);
                numChildren --;
            }
            else{
                pd.setPrice(startAdult + extra);
                numAdults --;
            }
        }
    }
    
    
    private void calculateTotalBookingPrice(){
        /**
         * Calculate the total price of the booking/flight.
         */
        double totalPrice = 0;
        for (PassengerInfo pd: this.passengerList){
            totalPrice += pd.getPrice();
        }
        
        this.totalBookingPrice = totalPrice;
        this.labelTotalBookingPrice.setText("Total booking price: "
                + String.format("%.2f", totalPrice)
                + Utils.EURO);
    }
    
    
    
    /**
     * Setup functions for passenger and seat panel, as their look
     * depends on the number of passengers.
     */
    
    private void setupPassengerPanel(){
        /**
         * After the button "Search" is pressed on the first panel, we need to add 
         * as many PassengerInfo objects/panels as there have been passengers selected. 
         * We add these objects to the base passenger panel.
         * 
         * Likewise, calculate the initial price for each passenger.
         */
        
        // Clear all components of the base passenger panel and all stored passengers. 
        // This is irrelevant on first setup, but if number of passengers were changed 
        // in the meantime, this ensures no excess PassengerInfo objects are added, and
        // no excess passengers are stored in the ArrayList.
        this.basePanelPsg.removeAll();
        this.passengerList.clear();
        
        // Total number of passengers
        int numAdults = (int) this.spinnerAdults.getValue();
        int numChildren = (int) this.spinnerChildren.getValue();
        this.numAdultsSelected = numAdults;
        this.numChildrenSelected = numChildren;
        int numPsg = numAdults + numChildren;
        int numCards = (int)Math.ceil(numPsg / 2.0);
        
        
        for (int i = 0; i < numCards; i ++){
            // Create a panel which will accommodate 1 or 2 PassengerInfo objects
            // Why only this much - because for the current frame size, max 2 of these
            // objects can fit. I also don't want to bother with resizing stuff.
            JPanel p = new JPanel();
            p.setName(String.valueOf(i));
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
//            p.setBackground(App.BACKGROUND_COLOR);
            
            if (numPsg >= 2){
                // we can add two objects
                PassengerInfo pd1 = new PassengerInfo(i*2 + 1); 
                PassengerInfo pd2 = new PassengerInfo(i*2 + 2);
                p.add(pd1);
                p.add(pd2);
                numPsg -= 2;
                
                // For now, adding these objects to passengerList doesn't do much except
                // that it allows us to create this many PassengerSeat objects which are
                // then put in their right place on the base seat panel.
                // Later, we will update these objects to hold "actual" passengers, not
                // just placeholder ones, so to say.
                this.passengerList.add(pd1);
                this.passengerList.add(pd2);
            }
            else {
                PassengerInfo pd1 = new PassengerInfo(i*2 + 1);
                p.add(pd1);
                p.add(Box.createVerticalStrut(this.getHeight())); // gets rid of the extra high border
                numPsg -= 1; // 0 passengers remain

                // Same argument as above
                this.passengerList.add(pd1);
            }
            this.basePanelPsg.add(p);
        }
    }
    
    
    private void setupSeatPanel(){
        /**
         * Setup the seat panel - remove all potentially previously added
         * PassengerSeat objects, and add new ones, each of which has an
         * accompanying PassengerInfo object.
         */
        
        // Since this function is invoked only when the passenger panel
        // has been changed, we have to remove any previously added PassengerSeat
        // objects, so there are no excess.
        this.seatPanelPsgs.removeAll();
        
        for (int i = 0; i < this.passengerList.size(); i ++){
            PassengerInfo pd = this.passengerList.get(i); // at the moment, as stated in setupPassengerPanel(), these are just placeholder values
            this.seatPanelPsgs.add(new PassengerSeat(pd.getPassengerNumber(), pd.getPsgName(), pd.getSurname()));
        }
        
        // fill up the empty space, so all PassengerSeat objects get pushed to the top
        int passengerSeatHeight = PassengerSeat.BOTTOM_BORDER + PassengerSeat.PANEL_HEIGHT;
        int boxHeight = this.seatPanelPsgs.getHeight() - this.passengerList.size() * passengerSeatHeight;
        if (boxHeight > 0){
            this.seatPanelPsgs.add(Box.createVerticalStrut(boxHeight));
        }
    }
    
    
    
    /**
     * Input validation functions - check if all inputs are okay, so we can proceed.
     */
    
    private boolean checkFlightDetails(){
        /** 
         * Check if all initial flight details are okay.
         */
        
        // Check that flight type is selected
        if (!Utils.buttonFromGroupSelected(this.groupFlightType)){
            Utils.showErrorMessage(this, "Please select if this flight is one way or return, and try again.");
            return false;
        }
        boolean returnFlight = Utils.getSelectedRadioButtonName(this.groupFlightType).equals("Return");
        
        
        // Check that both cities are selected, and that they're different
        int selectedFromIndex = this.cboxFrom.getSelectedIndex();
        int selectedToIndex = this.cboxTo.getSelectedIndex();
        if (selectedFromIndex == 0){
            Utils.showErrorMessage(this, "There was no origin city selected. "
                    + "Please select a city to fly from, and try again.");
            return false;
        }
        if (selectedToIndex == 0){
            Utils.showErrorMessage(this, "There was no destination city selected. "
                    + "Please select a city to fly to, and try again.");
            return false;
        }
        if (selectedFromIndex == selectedToIndex){
            Utils.showErrorMessage(this, "Origin city cannot be the same as the destination city. "
                    + "Please change one or both cities, and try again.");
            return false;
        }
        
        
        // Check everything related to dates
        LocalDate today = LocalDate.now();
        
        // Make outbound date, and check if it's valid
        int outboundDay = Integer.parseInt((String)this.cboxOutboundDay.getSelectedItem());
        String outboundMonthS = (String)this.cboxOutboundMonth.getSelectedItem();
        int outboundMonth = Month.valueOf(outboundMonthS.toUpperCase()).getValue();
        int outboundYear = Integer.parseInt((String)this.cboxOutboundYear.getSelectedItem());
        LocalDate outboundDate;
        try {
            outboundDate = LocalDate.of(outboundYear, outboundMonth, outboundDay);
        } catch (DateTimeException e){
            String parsed = Utils.parseInvalidDateMessage(e.getMessage());
            Utils.showErrorMessage(this, "Chosen outbound month and day combination (" + 
                    parsed + ") is invalid. Please select a valid combination and try again.");
            return false;
        }
        
        // Make return date, and check if it's valid
        int returnDay = Integer.parseInt((String)this.cboxReturnDay.getSelectedItem());
        String returnMonthS = (String)this.cboxReturnMonth.getSelectedItem();
        int returnMonth = Month.valueOf(returnMonthS.toUpperCase()).getValue();
        int returnYear = Integer.parseInt((String)this.cboxReturnYear.getSelectedItem());
        LocalDate returnDate;
        try {
            returnDate = LocalDate.of(returnYear, returnMonth, returnDay);
        } catch (DateTimeException e){
            String parsed = Utils.parseInvalidDateMessage(e.getMessage());
            Utils.showErrorMessage(this, "Chosen return month and day combination (" + 
                    parsed + ") is invalid. Please select a valid combination and try again.");
            return false;
        }
        
        if (outboundDate.isBefore(today)){
            Utils.showErrorMessage(this, "Chosen outbound date (" + outboundDate + ") is "
                    + "set before today's date (" + today + "). Please select a valid"
                    + " outbound date and try again.");
            return false;
        }
        
        // It's enough to check if return date <= outbound date,
        // we don't have to check if it's before today too.
        // Of course this only applies if the flight is infact
        // a return flight
        if (returnFlight){
            if (returnDate.isEqual(outboundDate)){
                Utils.showErrorMessage(this, "Chosen return date (" + returnDate + ") is "
                        + "set on the same day as the outbound date. "
                        + "Please select a later return date and try again.");
                return false;
            }
            if (returnDate.isBefore(outboundDate)){
                Utils.showErrorMessage(this, "Chosen return date (" + returnDate + ") is "
                        + "set before the outbound date (" + outboundDate + "). Please select a valid"
                        + " return date and try again.");
                return false;
            }
        }
        
        
        // Verify that time of flight and class are selected
        if (!Utils.buttonFromGroupSelected(this.groupFlightTime)){
            Utils.showErrorMessage(this, "Flight time was not selected. "
                    + "Please select your flight time (morning, afternoon, evening), and try again.");
            return false;
        }
        
        if (!Utils.buttonFromGroupSelected(this.groupFlightClass)){
            Utils.showErrorMessage(this, "Flight class was not selected. "
                    + "Please select your flight class (economy, business), and try again.");
            return false;
        }
        
        
        // Verify that there are a valid amount of passengers (>= 1)
        int numAdults = (int) this.spinnerAdults.getValue();
        int numChildren = (int) this.spinnerChildren.getValue();
        if (numAdults + numChildren == 0){
            Utils.showErrorMessage(this, "There has to be at least one adult or child passenger. "
                    + "Please select how many passengers there are, and try again.");
            return false;
        }
        
        
        // Everything is okay, yay
        return true;
    }
    
    
    private boolean checkPassengerInfo(int psgPanelIndex){
        /**
         * Check if the details for passengers in this subpanel are okay.
         * To avoid complicating, we will just check if all text fields 
         * are non empty, and whether a meal and luggage option were selected.
         * 
         * The parameter psgPanelIndex indicates on which subpanel, inside of 
         * the base passenger panel, we are on.
         */
        
        String passportRegex = "^P\\d{8}$";
        JPanel p = (JPanel)this.basePanelPsg.getComponent(psgPanelIndex); // get the current visible subpanel
        // ^ panel has either 2 components - they're both of type PassengerInfo, or one of those + a Box
        
        for (int i = 0; i < p.getComponentCount(); i ++){
            PassengerInfo pd;
            try {
                pd = (PassengerInfo)p.getComponent(i); // retrieve a single PassengerInfo subpanel
            }
            catch(ClassCastException e){
                // this is the vertical box/strut, so do nothing.
                continue;
            }
            
            int psgNumber = pd.getPassengerNumber();
            String psgName = pd.getPsgName();
            String psgSurname = pd.getSurname();
            String psgAddress = pd.getAddress();
            String psgCity = pd.getCity();
            String psgCountry = pd.getCountry();
            String psgPassport = pd.getPassport();
            String psgMeal = pd.getMeal();
            String psgLuggage = pd.getLuggage();
            
            // There's probably a more clever way for this
            if (psgName.equals("")){
                Utils.showErrorMessage(this, "Passenger " + psgNumber + " name is empty. "
                        + "Please input passenger " + psgNumber + "'s name and try again.");
                return false;
            }
            
            
            if (psgSurname.equals("")){
                Utils.showErrorMessage(this, "Passenger " + psgNumber + " surname is empty. "
                        + "Please input passenger " + psgNumber + "'s surname and try again.");
                return false;
            }
            
            
            if (psgAddress.equals("")){
                Utils.showErrorMessage(this, "Passenger " + psgNumber + " address is empty. "
                        + "Please input passenger " + psgNumber + "'s address and try again.");
                return false;
            }
            
            
            if (psgCity.equals("")){
                Utils.showErrorMessage(this, "Passenger " + psgNumber + " city is empty. "
                        + "Please input passenger " + psgNumber + "'s city and try again.");
                return false;
            }
            
            
            if (psgCountry.equals("")){
                Utils.showErrorMessage(this, "Passenger " + psgNumber + " country is empty. "
                        + "Please input passenger " + psgNumber + "'s country and try again.");
                return false;
            }
            
            
            if (psgPassport.equals("")){
                Utils.showErrorMessage(this, "Passenger " + psgNumber + " passport number is empty. "
                        + "Please input passenger " + psgNumber + "'s passport number and try again.");
                return false;
            }
            // Check if the passport is in a valid format
            boolean passportOk = Pattern.matches(passportRegex, psgPassport);
            if (!passportOk){
                Utils.showErrorMessage(this, "Passport number for passenger " + psgNumber + " "
                        + "is not in a valid format. The required format is \"P\" followed by 8 digits. "
                        + "You entered: " + psgPassport + ". Please enter a valid passport number for "
                        + "passenger " + psgNumber + " and try again.");
                return false;
            }
            
            
            if (psgMeal.equals("")){
                Utils.showErrorMessage(this, "Passenger " + psgNumber + " did not select a meal. "
                        + "Please select passenger " + psgNumber + "'s meal and try again.");
                return false;
            }
            
            
            if (psgLuggage.equals("")){
                Utils.showErrorMessage(this, "Passenger " + psgNumber + " did not select a luggage option. "
                        + "Please select passenger " + psgNumber + "'s luggage option and try again.");
                return false;
            }
        }
        
        return true;
    }
    
    
    private boolean checkSeatSelections(){
        /**
         * Check if all passengers have selected a seat.
         */
        
        for (int i = 0; i < this.seatPanelPsgs.getComponentCount(); i ++){
            PassengerSeat ps;
            try {
                ps = (PassengerSeat)this.seatPanelPsgs.getComponent(i);
            }
            catch (ClassCastException ex){
                // this is the box, so skip
                continue;
            }
            
            if (ps.getButtonSeat() == null){
                Utils.showErrorMessage(this, "Passenger " + ps.getPsgName() + " " + ps.getSurname() + " "
                        + "has not selected a seat. Please select one of the empty seats, and try again.");
                return false;
            }
        }
        
        return true;
    }
    
    
    private boolean checkPaymentDetails(){
        /**
         * Check if the payment details are okay. We are mainly interested 
         * in the credit card information for simplicity.
         */
        
        // Check all text fields are not empty
        String payerName = this.textPayerName.getText();
        String payerSurname = this.textPayerSurname.getText();
        String payerEmail = this.textPayerEmail.getText();
        String payerAddress = this.textPayerAddress.getText();
        String payerCity = this.textPayerCity.getText();
        String payerCountry = this.textPayerCountry.getText();
        String cardOwnerName = this.textCardOwnerName.getText();
        String cardNumber = this.textCardNumber.getText();
        String cardCVV = this.textCardCVV.getText();
        
        // There's probably a more clever way for this
        if (payerName.equals("")){
            Utils.showErrorMessage(this, "Payer name is empty. Please "
                    + "input a payer name and try again.");
            return false;
        }
        
        
        if (payerSurname.equals("")){
            Utils.showErrorMessage(this, "Payer surname is empty. Please "
                    + "input a payer surname and try again.");
            return false;
        }
        
        
        if (payerEmail.equals("")){
            Utils.showErrorMessage(this, "Payer email is empty. Please "
                    + "input a payer email and try again.");
            return false;
        }
        // Check that the email fits a pattern
        // https://howtodoinjava.com/java/regex/java-regex-validate-email-address/
        String emailRegex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        boolean emailOk = Pattern.matches(emailRegex, payerEmail);
        if (!emailOk){
            Utils.showErrorMessage(this, "Entered email is not in a valid format. Examples of valid emails "
                    + "are user@domain.com, user@domain.co.in, user.name@domain.com, user_name@domain.corporate.in. "
                    + "You entered: " + payerEmail + ". Please enter a valid email and try again.");
            return false;
        }
        
        
        if (payerAddress.equals("")){
            Utils.showErrorMessage(this, "Payer street address is empty. Please "
                    + "input a payer street address and try again.");
            return false;
        }
        
        
        if (payerCity.equals("")){
            Utils.showErrorMessage(this, "Payer city is empty. Please "
                    + "input a payer city and try again.");
            return false;
        }
        
        
        if (payerCountry.equals("")){
            Utils.showErrorMessage(this, "Payer country is empty. Please "
                    + "input a payer country and try again.");
            return false;
        }
        
        
        if (cardOwnerName.equals("")){
            Utils.showErrorMessage(this, "Card owner name and surname are empty. Please "
                    + "input the card owner's name and surname and try again.");
            return false;
        }
        
        
        // Check that the card number and CVV text fields contain only numbers
        // and that the format is okay
        String numberRegexSimple = "^\\d{16}$";
        String numberRegexBlanco = "^\\d{4}\\s\\d{4}\\s\\d{4}\\s\\d{4}$";
        String numberRegexDash = "^\\d{4}-\\d{4}-\\d{4}-\\d{4}$";
        String cvvRegex = "^\\d{3}$";
        
        if (cardNumber.equals("")){
            Utils.showErrorMessage(this, "Card number is empty. Please "
                    + "input a valid card number and try again.");
            return false;
        }
        
        boolean numberOk = 
                Pattern.matches(numberRegexSimple, cardNumber) || 
                Pattern.matches(numberRegexBlanco, cardNumber) || 
                Pattern.matches(numberRegexDash, cardNumber);
        
        if (!numberOk){
            Utils.showErrorMessage(this, "Card number is not in a valid format. "
                    + "It should contain 16 digits, with optional whitespace or - in between "
                    + "each group of 4. You entered: " + cardNumber + ". Please correct the "
                    + "entered card number and try again.");
            return false;
        }
        
        
        if (cardCVV.equals("")){
            Utils.showErrorMessage(this, "Card CVV is empty. Please "
                    + "input a valid card CVV and try again.");
            return false;
        }
        
        boolean cvvOk = Pattern.matches(cvvRegex, cardCVV);
        if (!cvvOk){
            Utils.showErrorMessage(this, "Card CVV is not in a valid format. "
                    + "It should contain only 3 digits. Please correct the entered "
                    + "CVV and try again.");
            return false;
        }
        
        
        // Check that month and date are not in the past.
        LocalDate today = LocalDate.now();
        
        int expMonth = Integer.parseInt((String)this.cboxExpirationMonth.getSelectedItem());
        int expYear = Integer.parseInt((String)this.cboxExpirationYear.getSelectedItem());
        int expDay = Month.of(expMonth).length(Year.of(expYear).isLeap()); // get max day for this month and year
        LocalDate expDate = LocalDate.of(expYear, expMonth, expDay);
        if (expDate.isBefore(today)){
            Utils.showErrorMessage(this, "Card expiration date ("
                    + this.cboxExpirationMonth.getSelectedItem() + " " + expYear + ") is "
                    + "set before today's date. Please choose a valid expiration date "
                    + "and try again.");
            return false;
        }
        
        return true;
    }
    
    
    
    /**
     * Functions that set the start state of each base panel.
     */
    
    private void mainPanelStartState(){
        /**
         * Set start state of the main panel.
         */
        
        // Set selection to ""
        this.cboxFrom.setSelectedIndex(0);
        this.cboxTo.setSelectedIndex(0);
        
        
        // Set dates to today for outbound, and tomorrow for (potential) return
        LocalDate today = LocalDate.now();
        
        this.cboxOutboundDay.setSelectedIndex(today.getDayOfMonth() - 1);
        this.cboxOutboundMonth.setSelectedIndex(today.getMonthValue() - 1);
        this.cboxOutboundYear.setSelectedIndex(today.getYear() - 2022);
        
        this.cboxReturnDay.setSelectedIndex(today.getDayOfMonth() % 31); // tomorrow; if it's the 31st, go to 1st next month
        this.cboxReturnMonth.setSelectedIndex(today.getMonthValue() - 1);
        this.cboxReturnYear.setSelectedIndex(today.getYear() - 2022);
        
        
        // Selection of return dates not possible when the app is started
        this.labelReturnDate.setEnabled(false);
        this.cboxReturnDay.setEnabled(false);
        this.cboxReturnMonth.setEnabled(false);
        this.cboxReturnYear.setEnabled(false);
        
        
        // Set passenger count to 1 for adults, and 0 for children
        this.spinnerAdults.setValue(1);
        this.spinnerChildren.setValue(0);
        
        
        // Unselect all radio buttons
        this.groupFlightType.clearSelection();
        this.groupFlightClass.clearSelection();
        this.groupFlightTime.clearSelection();
        
        
        // Set title
        this.changePanelTitle(0);
        
        
        // The back button is not enabled on the first panel
        this.buttonBack.setEnabled(false);
        
        
        // The "forward" button should say "Search"
        this.buttonForward.setText("Search");
    }
    
    
    private void passengerPanelStartState(){
        /**
         * Set start state of the (for simplicity sake) entire passenger panel.
         * This means we clear entered information on all subpanels.
         */
        int numSubpanels = this.basePanelPsg.getComponentCount();
        for (int i = 0; i < numSubpanels; i ++){
            // Subpanel has either 2 components - they're both of type PassengerInfo, or one of those + a Box
            JPanel p = (JPanel)this.basePanelPsg.getComponent(i);
            
            for (int j = 0; j < p.getComponentCount(); j ++){
                PassengerInfo pd;
                try {
                    pd = (PassengerInfo)p.getComponent(j); // retrieve a single PassengerInfo subpanel
                }
                catch(ClassCastException e){
                    // this is the vertical box/strut, so do nothing.
                    continue;
                }
                
                // reset all values
                pd.setPsgName("");
                pd.setSurname("");
                pd.setPassport("");
                pd.setAddress("");
                pd.setCity("");
                pd.setCountry("");
                pd.clearMeal();
                pd.clearLuggage();
                
                double oldPrice = pd.getPrice();
                double extraLuggage = pd.getLuggageExtraSelected() ? Utils.LUGGAGE_EXTRA : 0;
                pd.setLuggageExtraSelected(false);
                pd.setPrice(oldPrice - extraLuggage);
            }
        }
        
        // Bring us back to the first subpanel to make sure we will 
        // enter information for every passenger
        CardLayout c = (CardLayout)this.basePanelPsg.getLayout();
        c.first(this.basePanelPsg);
    }
    
    
    private void clearSeatSelections(){
        /**
         * Clear all seat selections.
         */
        for (Component c: this.seatPanelSeatTable.getComponents()){
            if (!(c instanceof JToggleButton)){
                continue;
            }
            JToggleButton t = (JToggleButton) c;
            t.setForeground(Color.WHITE);
            t.setText("-1-1"); // some random placeholder (invisible) text, just to keep all buttons the same size at all times
            t.setSelected(false);
        }
    }
    
    private void seatPanelStartState(){
        /**
         * Set start state of the seat panel.
         */
        
        // Clear seat selections
        this.clearSeatSelections();
		ActionPerformedSeatButton.restartSeats();
        
        
        // Reset PassengerSeat objects
        for (int i = 0; i < this.seatPanelPsgs.getComponentCount(); i ++){
            PassengerSeat ps;
            try {
                ps = (PassengerSeat)this.seatPanelPsgs.getComponent(i);
            } 
            catch (ClassCastException ex){
                // vertical box so continue;
                continue;
            }
            
            ps.setSelecting(false);
            ps.setLabelSeatText("Seat: (none)");
            ps.setButtonSeat(null);
            ps.setSeat("");
        }
    }
    
    
    private void paymentPanelStartState(){
        /**
         * Set start state of the payment panel.
         */
        
        this.cboxCopyFrom.setSelectedIndex(0);
        this.textPayerName.setText("");
        this.textPayerSurname.setText("");
        this.textPayerEmail.setText("");
        this.textPayerAddress.setText("");
        this.textPayerCity.setText("");
        this.textPayerCountry.setText("");
        this.textCardOwnerName.setText("");
        this.textCardNumber.setText("");
        this.textCardCVV.setText("");
        this.cboxExpirationMonth.setSelectedIndex(0);
        this.cboxExpirationYear.setSelectedIndex(1);
    }
    
    
    /**
     * Constructor
     */
    public MainFrame() {
        initComponents();
        this.additionalInit();
    }
    
    private void additionalInit(){
        /** 
         * Do some additional initializations.
         */
        
        // https://stackoverflow.com/a/70097948
//        JFrame.setDefaultLookAndFeelDecorated(true);
//        this.getRootPane().putClientProperty("JRootPane.titleBarBackground", App.BACKGROUND_COLOR);
//        this.getContentPane().setBackground(App.BACKGROUND_COLOR);
        
        // Insert empty selection into combo boxes
        this.cboxFrom.insertItemAt("", 0);
        this.cboxTo.insertItemAt("", 0);
        this.cboxCopyFrom.insertItemAt("", 0);
        
        // Set the main panel to its start state
        this.mainPanelStartState();
        
        // Add the item listener for flight type radio buttons
        this.radioOneWayFlight.addItemListener(new ItemListenerFlightType());
        this.radioReturnFlight.addItemListener(new ItemListenerFlightType());
        
        // Add an action listener for radio buttons corresponding to the seats
        for (Component c: this.seatPanelSeatTable.getComponents()){
            if (!(c instanceof JToggleButton)){
                continue;
            }
            JToggleButton t = (JToggleButton) c;
            t.addActionListener(new ActionPerformedSeatButton());
            t.addMouseListener(new MousePressedSeatButton());
            
            // Add some placeholder text, so that the buttons don't go bananas
            // with resizing themselves later, when we set their text to the 
            // initials of the passenger who selected this seat.
            t.setForeground(Color.WHITE);
            t.setText("AA");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        groupFlightType = new javax.swing.ButtonGroup();
        groupFlightTime = new javax.swing.ButtonGroup();
        groupFlightClass = new javax.swing.ButtonGroup();
        titlePanel = new javax.swing.JPanel();
        fillerLeft = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        labelPanelTitle = new javax.swing.JLabel();
        fillerRight = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        basePanel = new javax.swing.JPanel();
        basePanelMain = new javax.swing.JPanel();
        mainPanelButtons = new javax.swing.JPanel();
        radioOneWayFlight = new javax.swing.JRadioButton();
        radioReturnFlight = new javax.swing.JRadioButton();
        mainPanelDestinations = new javax.swing.JPanel();
        labelFrom = new javax.swing.JLabel();
        labelTo = new javax.swing.JLabel();
        labelReturnDate = new javax.swing.JLabel();
        labelOutboundDate = new javax.swing.JLabel();
        cboxFrom = new javax.swing.JComboBox<>();
        cboxTo = new javax.swing.JComboBox<>();
        cboxOutboundDay = new javax.swing.JComboBox<>();
        cboxOutboundMonth = new javax.swing.JComboBox<>();
        cboxOutboundYear = new javax.swing.JComboBox<>();
        cboxReturnDay = new javax.swing.JComboBox<>();
        cboxReturnMonth = new javax.swing.JComboBox<>();
        cboxReturnYear = new javax.swing.JComboBox<>();
        mainPanelTimeClass = new javax.swing.JPanel();
        labelFlightTime = new javax.swing.JLabel();
        labelFlightClass = new javax.swing.JLabel();
        radioFlightMorning = new javax.swing.JRadioButton();
        radioFlightAfternoon = new javax.swing.JRadioButton();
        radioFlightEvening = new javax.swing.JRadioButton();
        radioFlightEconomy = new javax.swing.JRadioButton();
        radioFlightBusiness = new javax.swing.JRadioButton();
        mainPanelPassengers = new javax.swing.JPanel();
        labelAdults = new javax.swing.JLabel();
        spinnerAdults = new javax.swing.JSpinner();
        labelChildren = new javax.swing.JLabel();
        spinnerChildren = new javax.swing.JSpinner();
        basePanelPsg = new javax.swing.JPanel();
        basePanelSeat = new javax.swing.JPanel();
        seatPanelContainer = new javax.swing.JPanel();
        seatPanelSeatTable = new javax.swing.JPanel();
        labelSeatColA = new javax.swing.JLabel();
        labelSeatColC = new javax.swing.JLabel();
        labelSeatColD = new javax.swing.JLabel();
        labelSeatColF = new javax.swing.JLabel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton2 = new javax.swing.JToggleButton();
        jToggleButton3 = new javax.swing.JToggleButton();
        jToggleButton4 = new javax.swing.JToggleButton();
        jToggleButton8 = new javax.swing.JToggleButton();
        jToggleButton7 = new javax.swing.JToggleButton();
        jToggleButton6 = new javax.swing.JToggleButton();
        jToggleButton5 = new javax.swing.JToggleButton();
        jToggleButton9 = new javax.swing.JToggleButton();
        jToggleButton11 = new javax.swing.JToggleButton();
        jToggleButton12 = new javax.swing.JToggleButton();
        jToggleButton10 = new javax.swing.JToggleButton();
        jToggleButton13 = new javax.swing.JToggleButton();
        jToggleButton14 = new javax.swing.JToggleButton();
        jToggleButton15 = new javax.swing.JToggleButton();
        jToggleButton16 = new javax.swing.JToggleButton();
        jToggleButton17 = new javax.swing.JToggleButton();
        jToggleButton18 = new javax.swing.JToggleButton();
        jToggleButton19 = new javax.swing.JToggleButton();
        jToggleButton20 = new javax.swing.JToggleButton();
        jToggleButton21 = new javax.swing.JToggleButton();
        jToggleButton22 = new javax.swing.JToggleButton();
        jToggleButton23 = new javax.swing.JToggleButton();
        jToggleButton24 = new javax.swing.JToggleButton();
        jToggleButton25 = new javax.swing.JToggleButton();
        jToggleButton26 = new javax.swing.JToggleButton();
        jToggleButton27 = new javax.swing.JToggleButton();
        jToggleButton28 = new javax.swing.JToggleButton();
        jToggleButton29 = new javax.swing.JToggleButton();
        jToggleButton30 = new javax.swing.JToggleButton();
        jToggleButton31 = new javax.swing.JToggleButton();
        jToggleButton32 = new javax.swing.JToggleButton();
        labelSeatRow1 = new javax.swing.JLabel();
        labelSeatRow2 = new javax.swing.JLabel();
        labelSeatRow3 = new javax.swing.JLabel();
        labelSeatRow4 = new javax.swing.JLabel();
        labelSeatRow5 = new javax.swing.JLabel();
        labelSeatRow6 = new javax.swing.JLabel();
        labelSeatRow7 = new javax.swing.JLabel();
        labelSeatRow8 = new javax.swing.JLabel();
        seatPanelPsgs = new javax.swing.JPanel();
        basePanelPayment = new javax.swing.JPanel();
        labelTotalBookingPrice = new javax.swing.JLabel();
        panelPayerDetailsContainer = new javax.swing.JPanel();
        labelPayerName = new javax.swing.JLabel();
        labelPayerSurname = new javax.swing.JLabel();
        labelPayerEmail = new javax.swing.JLabel();
        labelPayerAddress = new javax.swing.JLabel();
        labelPayerCity = new javax.swing.JLabel();
        labelPayerCountry = new javax.swing.JLabel();
        textPayerName = new javax.swing.JTextField();
        textPayerSurname = new javax.swing.JTextField();
        textPayerEmail = new javax.swing.JTextField();
        textPayerAddress = new javax.swing.JTextField();
        textPayerCity = new javax.swing.JTextField();
        textPayerCountry = new javax.swing.JTextField();
        labelCopyFrom = new javax.swing.JLabel();
        cboxCopyFrom = new javax.swing.JComboBox<>();
        panelCardDetailsContainer = new javax.swing.JPanel();
        labelCardOwnerName = new javax.swing.JLabel();
        labelCardNumber = new javax.swing.JLabel();
        labelCardExpiration = new javax.swing.JLabel();
        labelCardCVV = new javax.swing.JLabel();
        textCardOwnerName = new javax.swing.JTextField();
        textCardNumber = new javax.swing.JTextField();
        cboxExpirationMonth = new javax.swing.JComboBox<>();
        cboxExpirationYear = new javax.swing.JComboBox<>();
        textCardCVV = new javax.swing.JTextField();
        buttonsPanel = new javax.swing.JPanel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        buttonBack = new javax.swing.JButton();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(60, 0), new java.awt.Dimension(60, 32767));
        buttonClear = new javax.swing.JButton();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(60, 0), new java.awt.Dimension(60, 32767));
        buttonForward = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(" Book a flight");
        setBackground(new java.awt.Color(250, 250, 250));
        setMinimumSize(new java.awt.Dimension(120, 50));
        setName("frame"); // NOI18N
        setResizable(false);

        titlePanel.setPreferredSize(new java.awt.Dimension(670, 60));
        titlePanel.setLayout(new javax.swing.BoxLayout(titlePanel, javax.swing.BoxLayout.LINE_AXIS));
        titlePanel.add(fillerLeft);

        labelPanelTitle.setFont(new java.awt.Font("Lucida Sans", 0, 24)); // NOI18N
        labelPanelTitle.setText("TITLE");
        labelPanelTitle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        titlePanel.add(labelPanelTitle);
        titlePanel.add(fillerRight);

        basePanel.setName("basePanel"); // NOI18N
        basePanel.setPreferredSize(new java.awt.Dimension(670, 545));
        basePanel.setLayout(new java.awt.CardLayout());

        basePanelMain.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        basePanelMain.setPreferredSize(new java.awt.Dimension(670, 545));

        mainPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Trip", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans", 0, 18))); // NOI18N
        mainPanelButtons.setForeground(new java.awt.Color(40, 96, 241));
        mainPanelButtons.setPreferredSize(new java.awt.Dimension(670, 70));

        groupFlightType.add(radioOneWayFlight);
        radioOneWayFlight.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        radioOneWayFlight.setText("One way");
        radioOneWayFlight.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        radioOneWayFlight.setName("One way"); // NOI18N

        groupFlightType.add(radioReturnFlight);
        radioReturnFlight.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        radioReturnFlight.setText("Return");
        radioReturnFlight.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        radioReturnFlight.setName("Return"); // NOI18N

        javax.swing.GroupLayout mainPanelButtonsLayout = new javax.swing.GroupLayout(mainPanelButtons);
        mainPanelButtons.setLayout(mainPanelButtonsLayout);
        mainPanelButtonsLayout.setHorizontalGroup(
            mainPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(radioOneWayFlight)
                .addGap(144, 144, 144)
                .addComponent(radioReturnFlight)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        mainPanelButtonsLayout.setVerticalGroup(
            mainPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioOneWayFlight)
                    .addComponent(radioReturnFlight))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        mainPanelDestinations.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Destinations and dates", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans", 0, 18))); // NOI18N
        mainPanelDestinations.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        mainPanelDestinations.setPreferredSize(new java.awt.Dimension(670, 200));

        labelFrom.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelFrom.setText("From");

        labelTo.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelTo.setText("To");

        labelReturnDate.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelReturnDate.setText("Return date");

        labelOutboundDate.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelOutboundDate.setText("Outbound date");

        cboxFrom.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        cboxFrom.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Ljubljana", "Zagreb", "Podgorica", "Belgrade", "Sarajevo" }));
        cboxFrom.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        cboxFrom.setName("from"); // NOI18N
        cboxFrom.setPreferredSize(new java.awt.Dimension(72, 25));

        cboxTo.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        cboxTo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Ljubljana", "Zagreb", "Podgorica", "Belgrade", "Sarajevo" }));
        cboxTo.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        cboxTo.setName("to"); // NOI18N
        cboxTo.setPreferredSize(new java.awt.Dimension(72, 25));

        cboxOutboundDay.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        cboxOutboundDay.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }));
        cboxOutboundDay.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        cboxOutboundMonth.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        cboxOutboundMonth.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }));
        cboxOutboundMonth.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        cboxOutboundYear.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        cboxOutboundYear.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2022", "2023", "2024" }));
        cboxOutboundYear.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        cboxReturnDay.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        cboxReturnDay.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }));
        cboxReturnDay.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        cboxReturnMonth.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        cboxReturnMonth.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }));
        cboxReturnMonth.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        cboxReturnYear.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        cboxReturnYear.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2022", "2023", "2024" }));
        cboxReturnYear.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        javax.swing.GroupLayout mainPanelDestinationsLayout = new javax.swing.GroupLayout(mainPanelDestinations);
        mainPanelDestinations.setLayout(mainPanelDestinationsLayout);
        mainPanelDestinationsLayout.setHorizontalGroup(
            mainPanelDestinationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelDestinationsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelDestinationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelDestinationsLayout.createSequentialGroup()
                        .addGroup(mainPanelDestinationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelFrom)
                            .addComponent(labelOutboundDate))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(mainPanelDestinationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(mainPanelDestinationsLayout.createSequentialGroup()
                                .addComponent(labelReturnDate)
                                .addGap(220, 220, 220))
                            .addComponent(labelTo, javax.swing.GroupLayout.Alignment.LEADING)))
                    .addGroup(mainPanelDestinationsLayout.createSequentialGroup()
                        .addGroup(mainPanelDestinationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelDestinationsLayout.createSequentialGroup()
                                .addComponent(cboxOutboundDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cboxOutboundMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(mainPanelDestinationsLayout.createSequentialGroup()
                                .addGap(210, 210, 210)
                                .addComponent(cboxOutboundYear, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(28, 28, 28)
                        .addComponent(cboxReturnDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboxReturnMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboxReturnYear, 0, 86, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelDestinationsLayout.createSequentialGroup()
                        .addComponent(cboxFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cboxTo, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        mainPanelDestinationsLayout.setVerticalGroup(
            mainPanelDestinationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelDestinationsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelDestinationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelFrom)
                    .addComponent(labelTo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(mainPanelDestinationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboxFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboxTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(mainPanelDestinationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelOutboundDate, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelReturnDate, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelDestinationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelDestinationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cboxReturnDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cboxReturnYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cboxReturnMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainPanelDestinationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cboxOutboundDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cboxOutboundYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cboxOutboundMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(17, 17, 17))
        );

        mainPanelTimeClass.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Time and class", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans", 0, 18))); // NOI18N
        mainPanelTimeClass.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        mainPanelTimeClass.setPreferredSize(new java.awt.Dimension(670, 50));

        labelFlightTime.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelFlightTime.setText("Flight time");

        labelFlightClass.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelFlightClass.setText("Flight class");

        groupFlightTime.add(radioFlightMorning);
        radioFlightMorning.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        radioFlightMorning.setText("Morning");
        radioFlightMorning.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        radioFlightMorning.setName("morning"); // NOI18N

        groupFlightTime.add(radioFlightAfternoon);
        radioFlightAfternoon.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        radioFlightAfternoon.setText("Afternoon");
        radioFlightAfternoon.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        radioFlightAfternoon.setName("afternoon"); // NOI18N

        groupFlightTime.add(radioFlightEvening);
        radioFlightEvening.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        radioFlightEvening.setText("Evening");
        radioFlightEvening.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        radioFlightEvening.setName("evening"); // NOI18N

        groupFlightClass.add(radioFlightEconomy);
        radioFlightEconomy.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        radioFlightEconomy.setText("Economy");
        radioFlightEconomy.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        radioFlightEconomy.setName("economy"); // NOI18N

        groupFlightClass.add(radioFlightBusiness);
        radioFlightBusiness.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        radioFlightBusiness.setText("Business");
        radioFlightBusiness.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        radioFlightBusiness.setName("business"); // NOI18N

        javax.swing.GroupLayout mainPanelTimeClassLayout = new javax.swing.GroupLayout(mainPanelTimeClass);
        mainPanelTimeClass.setLayout(mainPanelTimeClassLayout);
        mainPanelTimeClassLayout.setHorizontalGroup(
            mainPanelTimeClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelTimeClassLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelTimeClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelTimeClassLayout.createSequentialGroup()
                        .addComponent(radioFlightEvening)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(mainPanelTimeClassLayout.createSequentialGroup()
                        .addGroup(mainPanelTimeClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radioFlightMorning)
                            .addComponent(radioFlightAfternoon)
                            .addComponent(labelFlightTime))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 108, Short.MAX_VALUE)
                        .addGroup(mainPanelTimeClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelTimeClassLayout.createSequentialGroup()
                                .addComponent(labelFlightClass)
                                .addGap(8, 8, 8))
                            .addComponent(radioFlightEconomy)
                            .addComponent(radioFlightBusiness))))
                .addContainerGap())
        );
        mainPanelTimeClassLayout.setVerticalGroup(
            mainPanelTimeClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelTimeClassLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelTimeClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelFlightTime)
                    .addComponent(labelFlightClass))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelTimeClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioFlightEconomy)
                    .addComponent(radioFlightMorning))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(mainPanelTimeClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radioFlightBusiness, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(radioFlightAfternoon, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioFlightEvening)
                .addGap(36, 36, 36))
        );

        mainPanelPassengers.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Passengers", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans", 0, 18))); // NOI18N
        mainPanelPassengers.setPreferredSize(new java.awt.Dimension(311, 159));

        labelAdults.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelAdults.setText("Adults");

        spinnerAdults.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        spinnerAdults.setModel(new javax.swing.SpinnerNumberModel(1, 0, null, 1));

        labelChildren.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelChildren.setText("Children");

        spinnerChildren.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        spinnerChildren.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        javax.swing.GroupLayout mainPanelPassengersLayout = new javax.swing.GroupLayout(mainPanelPassengers);
        mainPanelPassengers.setLayout(mainPanelPassengersLayout);
        mainPanelPassengersLayout.setHorizontalGroup(
            mainPanelPassengersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelPassengersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelPassengersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelAdults)
                    .addComponent(spinnerAdults, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelChildren)
                    .addComponent(spinnerChildren, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(211, Short.MAX_VALUE))
        );
        mainPanelPassengersLayout.setVerticalGroup(
            mainPanelPassengersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelPassengersLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelAdults)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spinnerAdults, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(labelChildren)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spinnerChildren, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout basePanelMainLayout = new javax.swing.GroupLayout(basePanelMain);
        basePanelMain.setLayout(basePanelMainLayout);
        basePanelMainLayout.setHorizontalGroup(
            basePanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basePanelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(basePanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(basePanelMainLayout.createSequentialGroup()
                        .addComponent(mainPanelTimeClass, javax.swing.GroupLayout.PREFERRED_SIZE, 316, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(mainPanelPassengers, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(mainPanelDestinations, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE)
                    .addComponent(mainPanelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE)))
        );
        basePanelMainLayout.setVerticalGroup(
            basePanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basePanelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(mainPanelDestinations, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(basePanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(mainPanelPassengers, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(mainPanelTimeClass, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        basePanel.add(basePanelMain, "mainPanel");

        basePanelPsg.setPreferredSize(new java.awt.Dimension(670, 545));
        basePanelPsg.setLayout(new java.awt.CardLayout());
        basePanel.add(basePanelPsg, "passengerPanel");

        basePanelSeat.setName("basePanelSeat"); // NOI18N

        seatPanelContainer.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans", 0, 18))); // NOI18N
        seatPanelContainer.setPreferredSize(new java.awt.Dimension(650, 485));

        seatPanelSeatTable.setName("seatTable"); // NOI18N
        seatPanelSeatTable.setLayout(new java.awt.GridBagLayout());

        labelSeatColA.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelSeatColA.setText("A");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 0.1;
        seatPanelSeatTable.add(labelSeatColA, gridBagConstraints);

        labelSeatColC.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelSeatColC.setText("C");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 0.1;
        seatPanelSeatTable.add(labelSeatColC, gridBagConstraints);

        labelSeatColD.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelSeatColD.setText("D");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 0.1;
        seatPanelSeatTable.add(labelSeatColD, gridBagConstraints);

        labelSeatColF.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelSeatColF.setText("F");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 0.1;
        seatPanelSeatTable.add(labelSeatColF, gridBagConstraints);

        jToggleButton1.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton1.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton1.setName("A1"); // NOI18N
        jToggleButton1.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton1, gridBagConstraints);

        jToggleButton2.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton2.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton2.setName("C1"); // NOI18N
        jToggleButton2.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton2, gridBagConstraints);

        jToggleButton3.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton3.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton3.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton3.setName("D1"); // NOI18N
        jToggleButton3.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton3, gridBagConstraints);

        jToggleButton4.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton4.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton4.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton4.setName("F1"); // NOI18N
        jToggleButton4.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton4, gridBagConstraints);

        jToggleButton8.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton8.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton8.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton8.setName("A2"); // NOI18N
        jToggleButton8.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton8, gridBagConstraints);

        jToggleButton7.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton7.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton7.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton7.setName("C2"); // NOI18N
        jToggleButton7.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton7, gridBagConstraints);

        jToggleButton6.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton6.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton6.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton6.setName("D2"); // NOI18N
        jToggleButton6.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton6, gridBagConstraints);
        jToggleButton6.getAccessibleContext().setAccessibleDescription("");

        jToggleButton5.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton5.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton5.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton5.setName("F2"); // NOI18N
        jToggleButton5.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton5, gridBagConstraints);

        jToggleButton9.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton9.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton9.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton9.setName("A3"); // NOI18N
        jToggleButton9.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton9, gridBagConstraints);

        jToggleButton11.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton11.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton11.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton11.setName("C3"); // NOI18N
        jToggleButton11.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton11, gridBagConstraints);

        jToggleButton12.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton12.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton12.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton12.setName("D3"); // NOI18N
        jToggleButton12.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton12, gridBagConstraints);

        jToggleButton10.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton10.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton10.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton10.setName("F3"); // NOI18N
        jToggleButton10.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton10, gridBagConstraints);

        jToggleButton13.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton13.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton13.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton13.setName("A4"); // NOI18N
        jToggleButton13.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton13, gridBagConstraints);

        jToggleButton14.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton14.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton14.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton14.setName("C4"); // NOI18N
        jToggleButton14.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton14, gridBagConstraints);

        jToggleButton15.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton15.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton15.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton15.setName("D4"); // NOI18N
        jToggleButton15.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton15, gridBagConstraints);

        jToggleButton16.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton16.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton16.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton16.setName("F4"); // NOI18N
        jToggleButton16.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton16, gridBagConstraints);

        jToggleButton17.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton17.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton17.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton17.setName("A5"); // NOI18N
        jToggleButton17.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton17, gridBagConstraints);

        jToggleButton18.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton18.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton18.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton18.setName("C5"); // NOI18N
        jToggleButton18.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton18, gridBagConstraints);

        jToggleButton19.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton19.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton19.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton19.setName("D5"); // NOI18N
        jToggleButton19.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton19, gridBagConstraints);

        jToggleButton20.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton20.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton20.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton20.setName("F5"); // NOI18N
        jToggleButton20.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton20, gridBagConstraints);

        jToggleButton21.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton21.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton21.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton21.setName("A6"); // NOI18N
        jToggleButton21.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton21, gridBagConstraints);

        jToggleButton22.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton22.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton22.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton22.setName("C6"); // NOI18N
        jToggleButton22.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton22, gridBagConstraints);

        jToggleButton23.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton23.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton23.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton23.setName("D6"); // NOI18N
        jToggleButton23.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton23, gridBagConstraints);

        jToggleButton24.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton24.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton24.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton24.setName("F6"); // NOI18N
        jToggleButton24.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton24, gridBagConstraints);

        jToggleButton25.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton25.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton25.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton25.setName("A7"); // NOI18N
        jToggleButton25.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton25, gridBagConstraints);

        jToggleButton26.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton26.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton26.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton26.setName("C7"); // NOI18N
        jToggleButton26.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton26, gridBagConstraints);

        jToggleButton27.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton27.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton27.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton27.setName("D7"); // NOI18N
        jToggleButton27.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton27, gridBagConstraints);

        jToggleButton28.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton28.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton28.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton28.setName("F7"); // NOI18N
        jToggleButton28.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton28, gridBagConstraints);

        jToggleButton29.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton29.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton29.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton29.setName("A8"); // NOI18N
        jToggleButton29.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton29, gridBagConstraints);

        jToggleButton30.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton30.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton30.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton30.setName("C8"); // NOI18N
        jToggleButton30.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton30, gridBagConstraints);

        jToggleButton31.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton31.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton31.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton31.setName("D8"); // NOI18N
        jToggleButton31.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton31, gridBagConstraints);

        jToggleButton32.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jToggleButton32.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToggleButton32.setMaximumSize(new java.awt.Dimension(30, 30));
        jToggleButton32.setName("F8"); // NOI18N
        jToggleButton32.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        seatPanelSeatTable.add(jToggleButton32, gridBagConstraints);

        labelSeatRow1.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelSeatRow1.setText("1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        seatPanelSeatTable.add(labelSeatRow1, gridBagConstraints);

        labelSeatRow2.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelSeatRow2.setText("2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        seatPanelSeatTable.add(labelSeatRow2, gridBagConstraints);

        labelSeatRow3.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelSeatRow3.setText("3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        seatPanelSeatTable.add(labelSeatRow3, gridBagConstraints);

        labelSeatRow4.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelSeatRow4.setText("4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        seatPanelSeatTable.add(labelSeatRow4, gridBagConstraints);

        labelSeatRow5.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelSeatRow5.setText("5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        seatPanelSeatTable.add(labelSeatRow5, gridBagConstraints);

        labelSeatRow6.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelSeatRow6.setText("6");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        seatPanelSeatTable.add(labelSeatRow6, gridBagConstraints);

        labelSeatRow7.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelSeatRow7.setText("7");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        seatPanelSeatTable.add(labelSeatRow7, gridBagConstraints);

        labelSeatRow8.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelSeatRow8.setText("8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        seatPanelSeatTable.add(labelSeatRow8, gridBagConstraints);

        seatPanelPsgs.setName("seatPanelPsgs"); // NOI18N
        seatPanelPsgs.setLayout(new javax.swing.BoxLayout(seatPanelPsgs, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.GroupLayout seatPanelContainerLayout = new javax.swing.GroupLayout(seatPanelContainer);
        seatPanelContainer.setLayout(seatPanelContainerLayout);
        seatPanelContainerLayout.setHorizontalGroup(
            seatPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, seatPanelContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(seatPanelSeatTable, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 128, Short.MAX_VALUE)
                .addComponent(seatPanelPsgs, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        seatPanelContainerLayout.setVerticalGroup(
            seatPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, seatPanelContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(seatPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(seatPanelSeatTable, javax.swing.GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE)
                    .addComponent(seatPanelPsgs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout basePanelSeatLayout = new javax.swing.GroupLayout(basePanelSeat);
        basePanelSeat.setLayout(basePanelSeatLayout);
        basePanelSeatLayout.setHorizontalGroup(
            basePanelSeatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basePanelSeatLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(seatPanelContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE)
                .addContainerGap())
        );
        basePanelSeatLayout.setVerticalGroup(
            basePanelSeatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basePanelSeatLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(seatPanelContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        basePanel.add(basePanelSeat, "card6");

        labelTotalBookingPrice.setFont(new java.awt.Font("Lucida Sans", 0, 18)); // NOI18N
        labelTotalBookingPrice.setText("Total booking price: 0.00€");

        panelPayerDetailsContainer.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Payer information", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans", 0, 18))); // NOI18N

        labelPayerName.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelPayerName.setText("Name");

        labelPayerSurname.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelPayerSurname.setText("Surname");

        labelPayerEmail.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelPayerEmail.setText("Email");
        labelPayerEmail.setToolTipText("<html>The email address where your booking and<br>\nreceipt will be sent. Examples of valid emails:\n<ul>\n<li>user@domain.com</li>\n<li>user@domain.co.in</li>\n<li>user.name@domain.com</li>\n<li>user_name@domain.corporate.in</li>\n</ul>\n</html>");

        labelPayerAddress.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelPayerAddress.setText("Street address");

        labelPayerCity.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelPayerCity.setText("City");

        labelPayerCountry.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelPayerCountry.setText("Country");

        textPayerName.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N

        textPayerSurname.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N

        textPayerEmail.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        textPayerEmail.setToolTipText("<html>The email address where your booking and<br>\nreceipt will be sent. Examples of valid emails:\n<ul>\n<li>user@domain.com</li>\n<li>user@domain.co.in</li>\n<li>user.name@domain.com</li>\n<li>user_name@domain.corporate.in</li>\n</ul>\n</html>");

        textPayerAddress.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N

        textPayerCity.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N

        textPayerCountry.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N

        labelCopyFrom.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelCopyFrom.setText("Copy from");

        cboxCopyFrom.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        cboxCopyFrom.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboxFillFromItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout panelPayerDetailsContainerLayout = new javax.swing.GroupLayout(panelPayerDetailsContainer);
        panelPayerDetailsContainer.setLayout(panelPayerDetailsContainerLayout);
        panelPayerDetailsContainerLayout.setHorizontalGroup(
            panelPayerDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPayerDetailsContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPayerDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPayerDetailsContainerLayout.createSequentialGroup()
                        .addGroup(panelPayerDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelPayerName)
                            .addComponent(labelPayerEmail)
                            .addComponent(labelPayerCity))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPayerDetailsContainerLayout.createSequentialGroup()
                        .addComponent(labelCopyFrom)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboxCopyFrom, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(textPayerCity)
                    .addComponent(textPayerEmail)
                    .addComponent(textPayerName, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(panelPayerDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(textPayerAddress, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                    .addComponent(labelPayerAddress)
                    .addComponent(labelPayerSurname)
                    .addComponent(textPayerSurname, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelPayerCountry)
                    .addComponent(textPayerCountry))
                .addContainerGap())
        );
        panelPayerDetailsContainerLayout.setVerticalGroup(
            panelPayerDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPayerDetailsContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPayerDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCopyFrom)
                    .addComponent(cboxCopyFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelPayerDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPayerName)
                    .addComponent(labelPayerSurname))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPayerDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textPayerName, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textPayerSurname, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelPayerDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPayerEmail)
                    .addComponent(labelPayerAddress))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPayerDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textPayerEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textPayerAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelPayerDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPayerCity)
                    .addComponent(labelPayerCountry))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPayerDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textPayerCity, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textPayerCountry, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelCardDetailsContainer.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Card details", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans", 0, 18))); // NOI18N

        labelCardOwnerName.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelCardOwnerName.setText("Full owner name");

        labelCardNumber.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelCardNumber.setText("Card number");
        labelCardNumber.setToolTipText("<html>\nThe card number is the 16 digit number at the<br>\nfront of your debit/credit card. Card number<br> \nallowed formats:\n<ul>\n<li>1234123412341234</li>\n<li>1234-1234-1234-1234</li>\n<li>1234 1234 1234 1234</li>\n</ul>\n</html>");

        labelCardExpiration.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelCardExpiration.setText("Expiration date");

        labelCardCVV.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelCardCVV.setText("CVV");
        labelCardCVV.setToolTipText("CVV is the 3 digit number found at the back of your debit/credit card.");

        textCardOwnerName.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N

        textCardNumber.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        textCardNumber.setToolTipText("<html>\nThe card number is the 16 digit number at the<br>\nfront of your debit/credit card. Card number<br> \nallowed formats:\n<ul>\n<li>1234123412341234</li>\n<li>1234-1234-1234-1234</li>\n<li>1234 1234 1234 1234</li>\n</ul>\n</html>");

        cboxExpirationMonth.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        cboxExpirationMonth.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" }));

        cboxExpirationYear.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        cboxExpirationYear.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2022", "2023", "2024", "2025" }));
        cboxExpirationYear.setSelectedIndex(1);

        textCardCVV.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        textCardCVV.setToolTipText("CVV is the 3 digit number found at the back of your debit/credit card.");

        javax.swing.GroupLayout panelCardDetailsContainerLayout = new javax.swing.GroupLayout(panelCardDetailsContainer);
        panelCardDetailsContainer.setLayout(panelCardDetailsContainerLayout);
        panelCardDetailsContainerLayout.setHorizontalGroup(
            panelCardDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardDetailsContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCardDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textCardOwnerName)
                    .addGroup(panelCardDetailsContainerLayout.createSequentialGroup()
                        .addGroup(panelCardDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelCardOwnerName)
                            .addComponent(labelCardExpiration))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(panelCardDetailsContainerLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(cboxExpirationMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cboxExpirationYear, 0, 150, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(panelCardDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCardDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(textCardNumber, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                        .addComponent(textCardCVV))
                    .addComponent(labelCardCVV)
                    .addComponent(labelCardNumber))
                .addContainerGap())
        );
        panelCardDetailsContainerLayout.setVerticalGroup(
            panelCardDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardDetailsContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCardDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCardNumber)
                    .addComponent(labelCardOwnerName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCardDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textCardNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textCardOwnerName, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelCardDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCardExpiration)
                    .addComponent(labelCardCVV))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCardDetailsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboxExpirationMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboxExpirationYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textCardCVV, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout basePanelPaymentLayout = new javax.swing.GroupLayout(basePanelPayment);
        basePanelPayment.setLayout(basePanelPaymentLayout);
        basePanelPaymentLayout.setHorizontalGroup(
            basePanelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basePanelPaymentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(basePanelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(basePanelPaymentLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(labelTotalBookingPrice)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, basePanelPaymentLayout.createSequentialGroup()
                        .addGroup(basePanelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelPayerDetailsContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panelCardDetailsContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        basePanelPaymentLayout.setVerticalGroup(
            basePanelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basePanelPaymentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelTotalBookingPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelPayerDetailsContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(panelCardDetailsContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        basePanel.add(basePanelPayment, "card5");

        buttonsPanel.setPreferredSize(new java.awt.Dimension(670, 50));
        buttonsPanel.setLayout(new javax.swing.BoxLayout(buttonsPanel, javax.swing.BoxLayout.X_AXIS));
        buttonsPanel.add(filler1);

        buttonBack.setBackground(new java.awt.Color(103, 218, 255));
        buttonBack.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        buttonBack.setText("Back");
        buttonBack.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        buttonBack.setPreferredSize(new java.awt.Dimension(120, 23));
        buttonBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonBackActionPerformed(evt);
            }
        });
        buttonsPanel.add(buttonBack);
        buttonsPanel.add(filler4);

        buttonClear.setBackground(new java.awt.Color(103, 218, 255));
        buttonClear.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        buttonClear.setText("Clear");
        buttonClear.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        buttonClear.setPreferredSize(new java.awt.Dimension(120, 23));
        buttonClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonClearActionPerformed(evt);
            }
        });
        buttonsPanel.add(buttonClear);
        buttonsPanel.add(filler5);

        buttonForward.setBackground(new java.awt.Color(103, 218, 255));
        buttonForward.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        buttonForward.setText("Search");
        buttonForward.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        buttonForward.setPreferredSize(new java.awt.Dimension(120, 23));
        buttonForward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonForwardActionPerformed(evt);
            }
        });
        buttonsPanel.add(buttonForward);
        buttonsPanel.add(filler3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(buttonsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addComponent(basePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addContainerGap())
                .addComponent(titlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(titlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(basePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 497, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    
    /**
     * Various event handlers
     */
    
    private void buttonBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonBackActionPerformed
        /**
         * Based on the current (sub)panel, go back to the previous panel.
         */

        // Note: "base" panel means one of the 4 base panels - "main", "passenger",
        // "seat", "payment".
        
        boolean previousBase = true; // whether we should go back to the previous base panel
        int panelIdx = this.currentVisiblePanelIndex(this.basePanel); // what base panel we were on
        switch (panelIdx){
            case 0 -> { // main panel
                System.out.println("THIS SHOULD NEVER HAPPEN AAAAAAAAAAAAAAAAAAA");
            }
            case 1 -> { // passenger panel
                int psgPanelIdx = this.currentVisiblePanelIndex(this.basePanelPsg);
                if (psgPanelIdx != 0){
                    // we're not on the first subpanel on the passenger panel,
                    // so we stay on it, and we go back one subpanel
                    CardLayout c1 = (CardLayout)this.basePanelPsg.getLayout();
                    c1.previous(this.basePanelPsg);
                    previousBase = false;
                }
                else{
                    // disable since we're actually going back to the main panel
                    this.buttonBack.setEnabled(false);
                    this.buttonForward.setText("Search");
                }
            }
            case 2 -> { // seat panel
                // do nothing
            }
            case 3 -> { // payment panel
                // do nothing else
                this.buttonForward.setText("Continue");
            }
        }
        if (previousBase){
            this.changePanelTitle(panelIdx - 1);
            CardLayout c = (CardLayout)this.basePanel.getLayout();
            c.previous(this.basePanel);
        }
    }//GEN-LAST:event_buttonBackActionPerformed

    
    private void buttonClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonClearActionPerformed
        /**
         * Clear all entered and selected information on the current panel.
         */
        
        int panelIdx = this.currentVisiblePanelIndex(this.basePanel);
        String msg = panelIdx == 1 ? "<html>Are you sure you want to clear entered information for "
                + "<b><u>all</u></b> passengers?</html>" : 
                "Are you sure you want to clear the entered information?";
        
        int result = Utils.showConfirmationMessage(this, msg, "Confirm clear", true);
        
        if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION
                || result == JOptionPane.CLOSED_OPTION){
            // user doesn't want to clear
            return;
        }
        
        switch (panelIdx){
            case 0 -> { // main panel
                this.mainPanelStartState();
            }
            case 1 -> { // passenger panel
                this.passengerPanelStartState();
            }
            case 2 -> { // seat panel
                this.seatPanelStartState();
            }
            case 3 -> { // payment panel
                this.paymentPanelStartState();
            }
        }
    }//GEN-LAST:event_buttonClearActionPerformed

    
    private void buttonForwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonForwardActionPerformed
        /**
         * Based on the current (sub)panel, go to the next (sub)panel.
         */

        // Note: "base" panel means one of the 4 base panels - "main", "passenger",
        // "seat", "payment". 
        
        boolean nextBase = true; // whether we should go the next base panel
        boolean completeOrder = false; // whether we should finalize the "order"
        int panelIdx = this.currentVisiblePanelIndex(this.basePanel); // what base panel we were on
        
        switch (panelIdx){
            case 0 -> { // main panel
                if (!this.checkFlightDetails()){
                    return;
                }
                
                /*
                To make sure no duplications are made, we remembered the selected amount
                of adult and child passengers separately. If we ever go back to the
                first panel, and we "search" again, we check if those stored amounts
                have changed. If not, we don't do anything (unless this is the first
                time we're searching), and if they did, we setup the panel from scratch.
                */
                
                int numAdults = (int)this.spinnerAdults.getValue();
                int numChildren = (int)this.spinnerChildren.getValue();
                if ((this.numAdultsSelected == -1) ||  // first time setup
                    !(this.numAdultsSelected == numAdults && this.numChildrenSelected == numChildren)){ // values changed
                    
                    this.numAdultsSelected = numAdults;
                    this.numChildrenSelected = numChildren;
                    
                    // The passenger panel is set up here - either it's the first time,
                    // or the number of adult and child passengers have changed.
                    this.setupPassengerPanel();
                    
                    // We set up the seat panel here - we add PassengerSeat objects 
                    // in their right place. However, since passenger information has not
                    // yet been entered, these PassengerSeat objects are just used to
                    // put as many of them as we need on the seat panel.
                    // Later, when we leave the passenger panel entirely, we will update
                    // the passengerList, and also the PassengerSeat objects to accommodate
                    // "actual" passengers.
                    this.setupSeatPanel();
                    
                    // We also clear any "dangling" seat selections - when setting up for
                    // the first time, this is irrelevant, but if the number of adult and
                    // child passengers have changed, this is important, otherwise we will
                    // have selected seats for non existent passengers.
                    this.clearSeatSelections();
                }
                
                // Some info on the flight details may have changed so (re)calculate
                // the initial passenger prices
                this.calculateInitialPassengerPrice();
                this.buttonBack.setEnabled(true); // we can click this button now
                this.buttonForward.setText("Continue");
            }
            
            case 1 -> { // passenger panel
                int psgPanelIdx = this.currentVisiblePanelIndex(this.basePanelPsg); // which subpanel we're on
                if(!this.checkPassengerInfo(psgPanelIdx)){
                    return;
                }
                if (psgPanelIdx + 1 != this.basePanelPsg.getComponentCount()){
                    // we stay on the passenger panel, since it's not the last subpanel,
                    // so we advance to the next one
                    nextBase = false;
                    CardLayout c1 = (CardLayout)this.basePanelPsg.getLayout();
                    c1.next(this.basePanelPsg);
                }
                // else nextBase = true, since this is the last subpanel where we 
                // fill in passenger info, so we continue onwards to pick the seats.
                // We also don't want to go to the "next" subpanel, because
                // it will be easier to go back later.
                else {
                    // update the passengerList here, since all passenger info has been entered
                    this.updatePassengersList();
                }
            }
            
            case 2 -> { // seat panel
                if (!this.checkSeatSelections()){
                    return;
                }
                
                this.buttonForward.setText("Checkout");
                    
                // Fill up the copyFrom combo box, found on the payment panel
                this.fillCopyFromComboBox();
                
                // Calculate the total price of the booking/flight
                this.calculateTotalBookingPrice();
                
                // Fill up the seat array list
                this.fillUpPassengerSeatMap();
            }
            
            case 3 -> { // payment panel
                if (!this.checkPaymentDetails()){
                    return;
                }
                completeOrder = true; // we can now complete the order!
            }

        }
        
        // Let's see what happens now
        if (!nextBase){
            // we still stay on the passenger panel
            return;
        }
        if (!completeOrder){
            // go forward
            this.changePanelTitle(panelIdx + 1);
            CardLayout c = (CardLayout)this.basePanel.getLayout();
            c.next(this.basePanel);       
        }
        else{
            // finalize the order!
            boolean result = this.proceedWithBooking();
            if (!result){
                return;
            }
            
            // Booking complete
            Utils.showOrderConfirmedInfoMessage(this, "Booking complete! Your tickets "
                    + "and receipt have been sent to email: " + this.textPayerEmail.getText() + ". "
                    + "Thank you for flying with Polet!");
                    
            // Reset all panels and necessary variables
            this.mainPanelStartState();
            this.passengerPanelStartState();
            this.seatPanelStartState();
            this.paymentPanelStartState();
            this.numAdultsSelected = -1;
            this.numChildrenSelected = -1;
            
            // Go back to the first panel
            this.changePanelTitle(0); // we'll go to 0
            CardLayout c = (CardLayout)this.basePanel.getLayout();
            c.first(this.basePanel); 
        }
    }//GEN-LAST:event_buttonForwardActionPerformed

    private void cboxFillFromItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxFillFromItemStateChanged
        /**
         * Process what happens when a passenger is selected from
         * the combo box on the payment panel.
         */
        if (evt.getStateChange() == ItemEvent.SELECTED){
            int selectedIndex = this.cboxCopyFrom.getSelectedIndex();
            if (selectedIndex == 0){
                // can be commented out if this is not the wanted behaviour
                this.textPayerName.setText("");
                this.textPayerSurname.setText("");
                this.textPayerAddress.setText("");
                this.textPayerCity.setText("");
                this.textPayerCountry.setText("");
            }
            else{
                PassengerInfo psg = this.passengerList.get(selectedIndex - 1);

                this.textPayerName.setText(psg.getPsgName());
                this.textPayerSurname.setText(psg.getSurname());
                this.textPayerAddress.setText(psg.getAddress());
                this.textPayerCity.setText(psg.getCity());
                this.textPayerCountry.setText(psg.getCountry());
            }
        }
    }//GEN-LAST:event_cboxFillFromItemStateChanged

    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        // Set FlatLaf Light look and feel. FlatLaf is added as a Gradle dependency.
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel basePanel;
    private javax.swing.JPanel basePanelMain;
    private javax.swing.JPanel basePanelPayment;
    private javax.swing.JPanel basePanelPsg;
    private javax.swing.JPanel basePanelSeat;
    private javax.swing.JButton buttonBack;
    private javax.swing.JButton buttonClear;
    private javax.swing.JButton buttonForward;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JComboBox<String> cboxCopyFrom;
    private javax.swing.JComboBox<String> cboxExpirationMonth;
    private javax.swing.JComboBox<String> cboxExpirationYear;
    private javax.swing.JComboBox<String> cboxFrom;
    private javax.swing.JComboBox<String> cboxOutboundDay;
    private javax.swing.JComboBox<String> cboxOutboundMonth;
    private javax.swing.JComboBox<String> cboxOutboundYear;
    private javax.swing.JComboBox<String> cboxReturnDay;
    private javax.swing.JComboBox<String> cboxReturnMonth;
    private javax.swing.JComboBox<String> cboxReturnYear;
    private javax.swing.JComboBox<String> cboxTo;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler fillerLeft;
    private javax.swing.Box.Filler fillerRight;
    private javax.swing.ButtonGroup groupFlightClass;
    private javax.swing.ButtonGroup groupFlightTime;
    private javax.swing.ButtonGroup groupFlightType;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton10;
    private javax.swing.JToggleButton jToggleButton11;
    private javax.swing.JToggleButton jToggleButton12;
    private javax.swing.JToggleButton jToggleButton13;
    private javax.swing.JToggleButton jToggleButton14;
    private javax.swing.JToggleButton jToggleButton15;
    private javax.swing.JToggleButton jToggleButton16;
    private javax.swing.JToggleButton jToggleButton17;
    private javax.swing.JToggleButton jToggleButton18;
    private javax.swing.JToggleButton jToggleButton19;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton20;
    private javax.swing.JToggleButton jToggleButton21;
    private javax.swing.JToggleButton jToggleButton22;
    private javax.swing.JToggleButton jToggleButton23;
    private javax.swing.JToggleButton jToggleButton24;
    private javax.swing.JToggleButton jToggleButton25;
    private javax.swing.JToggleButton jToggleButton26;
    private javax.swing.JToggleButton jToggleButton27;
    private javax.swing.JToggleButton jToggleButton28;
    private javax.swing.JToggleButton jToggleButton29;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToggleButton jToggleButton30;
    private javax.swing.JToggleButton jToggleButton31;
    private javax.swing.JToggleButton jToggleButton32;
    private javax.swing.JToggleButton jToggleButton4;
    private javax.swing.JToggleButton jToggleButton5;
    private javax.swing.JToggleButton jToggleButton6;
    private javax.swing.JToggleButton jToggleButton7;
    private javax.swing.JToggleButton jToggleButton8;
    private javax.swing.JToggleButton jToggleButton9;
    private javax.swing.JLabel labelAdults;
    private javax.swing.JLabel labelCardCVV;
    private javax.swing.JLabel labelCardExpiration;
    private javax.swing.JLabel labelCardNumber;
    private javax.swing.JLabel labelCardOwnerName;
    private javax.swing.JLabel labelChildren;
    private javax.swing.JLabel labelCopyFrom;
    private javax.swing.JLabel labelFlightClass;
    private javax.swing.JLabel labelFlightTime;
    private javax.swing.JLabel labelFrom;
    private javax.swing.JLabel labelOutboundDate;
    private javax.swing.JLabel labelPanelTitle;
    private javax.swing.JLabel labelPayerAddress;
    private javax.swing.JLabel labelPayerCity;
    private javax.swing.JLabel labelPayerCountry;
    private javax.swing.JLabel labelPayerEmail;
    private javax.swing.JLabel labelPayerName;
    private javax.swing.JLabel labelPayerSurname;
    private javax.swing.JLabel labelReturnDate;
    private javax.swing.JLabel labelSeatColA;
    private javax.swing.JLabel labelSeatColC;
    private javax.swing.JLabel labelSeatColD;
    private javax.swing.JLabel labelSeatColF;
    private javax.swing.JLabel labelSeatRow1;
    private javax.swing.JLabel labelSeatRow2;
    private javax.swing.JLabel labelSeatRow3;
    private javax.swing.JLabel labelSeatRow4;
    private javax.swing.JLabel labelSeatRow5;
    private javax.swing.JLabel labelSeatRow6;
    private javax.swing.JLabel labelSeatRow7;
    private javax.swing.JLabel labelSeatRow8;
    private javax.swing.JLabel labelTo;
    private javax.swing.JLabel labelTotalBookingPrice;
    private javax.swing.JPanel mainPanelButtons;
    private javax.swing.JPanel mainPanelDestinations;
    private javax.swing.JPanel mainPanelPassengers;
    private javax.swing.JPanel mainPanelTimeClass;
    private javax.swing.JPanel panelCardDetailsContainer;
    private javax.swing.JPanel panelPayerDetailsContainer;
    private javax.swing.JRadioButton radioFlightAfternoon;
    private javax.swing.JRadioButton radioFlightBusiness;
    private javax.swing.JRadioButton radioFlightEconomy;
    private javax.swing.JRadioButton radioFlightEvening;
    private javax.swing.JRadioButton radioFlightMorning;
    private javax.swing.JRadioButton radioOneWayFlight;
    private javax.swing.JRadioButton radioReturnFlight;
    private javax.swing.JPanel seatPanelContainer;
    private javax.swing.JPanel seatPanelPsgs;
    private javax.swing.JPanel seatPanelSeatTable;
    private javax.swing.JSpinner spinnerAdults;
    private javax.swing.JSpinner spinnerChildren;
    private javax.swing.JTextField textCardCVV;
    private javax.swing.JTextField textCardNumber;
    private javax.swing.JTextField textCardOwnerName;
    private javax.swing.JTextField textPayerAddress;
    private javax.swing.JTextField textPayerCity;
    private javax.swing.JTextField textPayerCountry;
    private javax.swing.JTextField textPayerEmail;
    private javax.swing.JTextField textPayerName;
    private javax.swing.JTextField textPayerSurname;
    private javax.swing.JPanel titlePanel;
    // End of variables declaration//GEN-END:variables
}
