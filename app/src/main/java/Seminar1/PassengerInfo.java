/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package Seminar1;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Luka
 */
public class PassengerInfo extends javax.swing.JPanel {
    
    /**
     * Item state changed item listener for the luggage related radio buttons.
     */
    
    private class ItemListenerLuggage implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED){
                String luggage = Utils.getSelectedRadioButtonName(groupLuggage);
                if (luggage.equals("regular")){
                    if (luggageExtraSelected){
                        luggageExtraSelected = false;
                        setPrice(price - Utils.LUGGAGE_EXTRA);
                    }
                }
                else{
                    setPrice(price + Utils.LUGGAGE_EXTRA);
                    luggageExtraSelected = true;
                }
            }
        }
    }

    private final int passengerNumber;
    private double price;
    private boolean luggageExtraSelected = false;
    
    
    /**
     * Creates new form PassengerInfo
     * @param psgNumber
     */
    public PassengerInfo(int psgNumber) {
        initComponents();
        this.passengerNumber = psgNumber;
        this.setBorder(BorderFactory.createTitledBorder(null, "Passenger " + psgNumber, 
                TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
                App.FONT.deriveFont(18.0f)));
        
        this.radioLuggageExtra.addItemListener(new ItemListenerLuggage());
        this.radioLuggageReg.addItemListener(new ItemListenerLuggage());
    }
    
    @Override
    public String toString(){
        return "Passenger #" + this.getPassengerNumber() + "\n" 
                + "Name and surname: " + this.getPsgName() + " " + this.getSurname() + "\n"
                + "Address: " + this.getAddress() + "\n"
                + "City and country: " + this.getCity() + ", " + this.getCountry() + "\n"
                + "Extra luggage: " + (this.luggageExtraSelected ? "Yes\n" : "No\n")
                + "Flight price: " + String.format("%.2f", this.getPrice()) + Utils.EURO + "\n";
    }
    
    public String toHTMLString(){
        return "<b>Passenger #" + this.getPassengerNumber() + "</b><br>" 
                + "Name and surname: " + this.getPsgName() + " " + this.getSurname() + "<br>"
                + "Address: " + this.getAddress() + "<br>"
                + "City and country: " + this.getCity() + ", " + this.getCountry() + "<br>"
                + "Extra luggage: " + (this.luggageExtraSelected ? "Yes<br>" : "No<br>")
                + "Flight price: " + String.format("%.2f", this.getPrice()) + Utils.EURO + "<br>";
    }
    
    // Price
    public void setPrice(double price){
        this.price = price;
        this.labelPrice.setText(String.format("%.2f", this.price) + Utils.EURO);
    }
    
    public double getPrice(){
        return this.price;
    }
    
    
    // Luggage extra selected
    public boolean getLuggageExtraSelected(){
        return this.luggageExtraSelected;
    }
    
    public void setLuggageExtraSelected(boolean val){
        this.luggageExtraSelected = val;
    }
    
    
    // Passenger number
    public int getPassengerNumber(){
        return this.passengerNumber;
    }
    
    
    // Passenger name
    public String getPsgName(){
        return this.textName.getText();
    }
    
    public void setPsgName(String text){
        this.textName.setText(text);
    }
    
    
    // Passenger surname
    public String getSurname(){
        return this.textSurname.getText();
    }
    
    public void setSurname(String text){
        this.textSurname.setText(text);
    }
    
    
    // Passenger address
    public String getAddress(){
        return this.textAddress.getText();
    }
    
    public void setAddress(String text){
        this.textAddress.setText(text);
    }
    
    
    // Passenger city
    public String getCity(){
        return this.textCity.getText();
    }
    
    public void setCity(String text){
        this.textCity.setText(text);
    }
    
    
    // Passenger country
    public String getCountry(){
        return this.textCountry.getText();
    }
    
    public void setCountry(String text){
        this.textCountry.setText(text);
    }
    
    
    // Passenger passport
    public String getPassport(){
        return this.textPassport.getText();
    }
    
    public void setPassport(String text){
        this.textPassport.setText(text);
    }
    
    
    // Passenger meal
    public String getMeal(){
        return Utils.getSelectedRadioButtonName(this.groupMeal);
    }
    
    public void clearMeal(){
        this.groupMeal.clearSelection();
    }
    
    
    // Passenger luggage
    public String getLuggage(){
        return Utils.getSelectedRadioButtonName(this.groupLuggage);
    }
    
    public void clearLuggage(){
        this.groupLuggage.clearSelection();
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        groupMeal = new javax.swing.ButtonGroup();
        groupLuggage = new javax.swing.ButtonGroup();
        labelName = new javax.swing.JLabel();
        textName = new javax.swing.JTextField();
        labelSurname = new javax.swing.JLabel();
        textSurname = new javax.swing.JTextField();
        labelAddress = new javax.swing.JLabel();
        textAddress = new javax.swing.JTextField();
        labelCity = new javax.swing.JLabel();
        textCity = new javax.swing.JTextField();
        labelCountry = new javax.swing.JLabel();
        textCountry = new javax.swing.JTextField();
        labelPassport = new javax.swing.JLabel();
        textPassport = new javax.swing.JTextField();
        labelMeal = new javax.swing.JLabel();
        labelLuggage = new javax.swing.JLabel();
        radioMealClassic = new javax.swing.JRadioButton();
        radioMealVegi = new javax.swing.JRadioButton();
        radioLuggageReg = new javax.swing.JRadioButton();
        radioLuggageExtra = new javax.swing.JRadioButton();
        labelPriceTitle = new javax.swing.JLabel();
        labelPrice = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Passenger i", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans", 0, 18))); // NOI18N

        labelName.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelName.setText("Name");

        textName.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N

        labelSurname.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelSurname.setText("Surname");

        textSurname.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N

        labelAddress.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelAddress.setText("Address");

        textAddress.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N

        labelCity.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelCity.setText("City");

        textCity.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N

        labelCountry.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelCountry.setText("Country");

        textCountry.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N

        labelPassport.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelPassport.setText("Passport");
        labelPassport.setToolTipText("Passport number format: P12345678");

        textPassport.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        textPassport.setToolTipText("Passport number format: P12345678");

        labelMeal.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelMeal.setText("Meal");

        labelLuggage.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelLuggage.setText("Luggage");

        groupMeal.add(radioMealClassic);
        radioMealClassic.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        radioMealClassic.setText("Classic");
        radioMealClassic.setName("classic"); // NOI18N

        groupMeal.add(radioMealVegi);
        radioMealVegi.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        radioMealVegi.setText("Vegetarian");
        radioMealVegi.setName("vegi"); // NOI18N

        groupLuggage.add(radioLuggageReg);
        radioLuggageReg.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        radioLuggageReg.setText("Regular (20kg)");
        radioLuggageReg.setName("regular"); // NOI18N

        groupLuggage.add(radioLuggageExtra);
        radioLuggageExtra.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        radioLuggageExtra.setText("Extra (+15kg)");
        radioLuggageExtra.setName("extra"); // NOI18N

        labelPriceTitle.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelPriceTitle.setText("Price");

        labelPrice.setFont(new java.awt.Font("Lucida Sans", 0, 15)); // NOI18N
        labelPrice.setText("0.00€");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelCity)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(textName, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(labelName))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(labelSurname)
                                .addComponent(textSurname, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(radioMealClassic)
                                        .addComponent(radioMealVegi))
                                    .addGap(117, 117, 117))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(textCity)
                                    .addGap(12, 12, 12)))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(radioLuggageReg)
                                .addComponent(radioLuggageExtra)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelLuggage)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(textCountry)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(labelCountry)
                                            .addGap(0, 0, Short.MAX_VALUE)))))))
                    .addComponent(labelMeal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelPassport)
                            .addComponent(labelPrice)
                            .addComponent(labelPriceTitle))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(textAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelAddress, javax.swing.GroupLayout.Alignment.LEADING)))
                            .addComponent(textPassport))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelName)
                    .addComponent(labelSurname)
                    .addComponent(labelAddress))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textSurname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCity)
                    .addComponent(labelCountry)
                    .addComponent(labelPassport))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textPassport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(textCity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(textCountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelMeal)
                            .addComponent(labelLuggage))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(radioMealClassic)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioMealVegi))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(radioLuggageReg)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioLuggageExtra))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelPriceTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelPrice)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup groupLuggage;
    private javax.swing.ButtonGroup groupMeal;
    private javax.swing.JLabel labelAddress;
    private javax.swing.JLabel labelCity;
    private javax.swing.JLabel labelCountry;
    private javax.swing.JLabel labelLuggage;
    private javax.swing.JLabel labelMeal;
    private javax.swing.JLabel labelName;
    private javax.swing.JLabel labelPassport;
    private javax.swing.JLabel labelPrice;
    private javax.swing.JLabel labelPriceTitle;
    private javax.swing.JLabel labelSurname;
    private javax.swing.JRadioButton radioLuggageExtra;
    private javax.swing.JRadioButton radioLuggageReg;
    private javax.swing.JRadioButton radioMealClassic;
    private javax.swing.JRadioButton radioMealVegi;
    private javax.swing.JTextField textAddress;
    private javax.swing.JTextField textCity;
    private javax.swing.JTextField textCountry;
    private javax.swing.JTextField textName;
    private javax.swing.JTextField textPassport;
    private javax.swing.JTextField textSurname;
    // End of variables declaration//GEN-END:variables
}
