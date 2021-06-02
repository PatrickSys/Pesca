
import Exceptions.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/************************************************************************
 Made by        PatrickSys
 Date           25/05/2021
 Package        Fishing
 Description:
 ************************************************************************/


public class Pesca {
    private final String arxiusFolder = "arxius/";
    private final File usuarisFile = new File(arxiusFolder, "usuaris.txt").getAbsoluteFile();
    private final File usuarisTemp = new File(arxiusFolder, "tempusuaris.txt").getAbsoluteFile();
    private final File register = new File(arxiusFolder, "registres.txt").getAbsoluteFile();
    private final int hashtagspesquera = 5;
    private final int hashtagsUsuaris = 3;
    private final int registerHashtags = 6;

    public static void main(String[] args) throws IOException, UserAlreadySignedUpException, CredentialsNotCorrectException {
        Pesca pesca = new Pesca();
        pesca.menu();

    }

    public Pesca() throws IOException {
        this.usuarisFile.createNewFile();
    }

    private void menu () throws IOException, UserAlreadySignedUpException, CredentialsNotCorrectException {
        boolean exit = false;
        do {
            try{
            String option = showChoices();
                if (null == option){
                    throw new InputNotValidException("Per favor, tria una opcio valida");
                }
                switch (option) {
                    case "1":
                        registerUser();
                        break;

                    case "2":
                        deleteUser();
                        break;

                    case "3":
                        fishing();
                        break;

                    case "4":
                        showUserStatistics();
                        break;

                    case "5":
                        showGlobalStatistics();
                        break;
                    case "s":
                        exit = true;
                        break;

                    default:
                        throw new InputNotValidException("Per favor, tria una opcio valida");
                }

        } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
            } while (!exit);
    }

    /**
     * Asks input to the user, checks whether it exists or not, and writes to the registres.txt
     * @throws IOException from I/O Streams
     * @throws UserAlreadySignedUpException if the user already exists
     */
    private void registerUser() throws IOException, UserAlreadySignedUpException {
        OutputStream writer;
        String nom = inputData("Nom");
        if(null == nom || nom.isBlank()){
            return;
        }
        if (userAlreadyExists(nom)){
            throw new UserAlreadySignedUpException("L'usuari ja existeix");
        }
        JOptionPane.showMessageDialog(null, "Benvingut " + nom);
        String password = inputData("Contrasenya");
        if (null == password || password.isBlank()){
            return;
        }
        // Following two statements checks whether the file is empty therefore the outputsream appends to the file or not
        String firstLine = readLine(this.usuarisFile, 3, 1);
        writer = checkEmptyFile(firstLine, this.usuarisFile);
        // writes user to the register
        writeHashtag(writer);
        writeLine(nom, writer);
        writeHashtag(writer);
        writeLine(password, writer);
        writeHashtag(writer);
        writer.close();
    }

    /**
     * Also checks if the user exists
     * @throws IOException from Streams
     * @throws UserNotFoundException if the user doesn't exist
     * rewrites the users file without the user to delete using a temp swap file
     */
    private void deleteUser() throws IOException, UserNotFoundException {
        this.usuarisTemp.createNewFile();
        String line;
        String name = inputData("Nom");
        if (!userAlreadyExists(name)) {
            throw new UserNotFoundException("No te llicencia");
        }
        int lineCursor = 0;
        // Reads line by line seeking for the user to delete
        do {
            lineCursor++;
            line = readLine(this.usuarisFile, 3, lineCursor);
            String readName = readSubLine(line, 2, 0);
            if(readName.isBlank()){
                continue;
            }
            if(!readName.equals(name)) {
                rewriteLine(line, this.usuarisTemp);
            }
        }while(!line.isEmpty());

        //swaps files --System.gc ensures the file is "flushed" --
        this.usuarisFile.delete();
        System.gc();
        Files.move(Paths.get(this.usuarisTemp.toString()), Paths.get(this.usuarisFile.toString()));
    }

    /**
     * Asks the fisher to be used, hooks a fish and writes it always checking for append or not
     * @throws IOException from Streams
     * @throws CredentialsNotCorrectException if credentials given are not correct
     */
    private void fishing() throws IOException, CredentialsNotCorrectException{
        this.register.createNewFile();
        String username = inputData("Usuari:");
        if (null == username){
            return;
        }
        String password = inputData("Contrasenya:");
        if (null == password){
            return;
        }
        if(!credentialsAreCorrect(username, password)){
            throw new CredentialsNotCorrectException("Usuari o contrasenya incorrectes\n" +
                    "Recorda que distingeix entre majuscules i minuscules!!");
        }

        File pesqueraFile = inputPesquera();
        // Gets the line representing the fishing
        String fished = fishHooked(pesqueraFile, username);
        // reads the first line of the file to check whether it's empty
        String firstLine = readLine(pesqueraFile, this.hashtagspesquera, 1);
        // The o.s is different depending on whether file's empty or not
        OutputStream writer = checkEmptyFile(firstLine, this.register);
        //finally writes the line
        writeLine(fished, writer);
    }

    /**
     * Creates the UI, reads from the register file and puts the maximum fishings into a hashmap to further
     * show it to the user. Doesn't store, it's just for elemental purposes
     * @throws IOException from Streams
     * @throws CredentialsNotCorrectException --
     */
      //He intentat evitar usar el hashmap de String i fer servir un de <Integer, Double>, agafant com a clau
      //la línia a la que es troba el peix a la pesquera però és totalment inviable, ja que necessites
      //saber l'String del nom a comprovar i comparar si ja està al HashMap
    private void showUserStatistics() throws IOException, CredentialsNotCorrectException {
        //Initialize variables and Frame Container
        String line;
        int lineCursor = 0;
        HashMap <String, Double> maxFished = new HashMap<>();
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        frame.add(panel);
        frame.setSize(500, 500);
        centerFrameOnScreen(frame);
        String username = inputData("Usuari:");
        if (null == username){
            return;
        }
        String password = inputData("Contrasenya:");
        if (null == password){
            return;
        }
        if(!credentialsAreCorrect(username, password)){
            throw new CredentialsNotCorrectException("Usuari o contrasenya incorrectes");
        }

        //Reads all lines from the file and puts max weight of the fish
        do{
            lineCursor++;
            line = readLine(this.register, this.registerHashtags, lineCursor);
            // Only gets the desired user's fishings
            if (!getUsernameFromRegister(line).equals(username)){
                continue;
            }
            // gets the fish weight from the line read
            double fishWeight = getFishWeight(line);
            // gets the Map key (fish name) according to that weight
            String weightKey = getKeyByWeight(fishWeight, maxFished);

              // If the fish' weight is more than the current value on the weight key corresponding to the fish,
              // replaces it. Otherwise simply puts it

            if (fishWeight > maxFished.getOrDefault(weightKey, fishWeight)) {
                maxFished.put(weightKey, fishWeight);
            }
            else{
                maxFished.put(getFishNameOnRegister(line), fishWeight);
            }
        }while(!line.isEmpty());
        // finally creates the UI representation for it
        mapToLabel(maxFished, frame, panel, lineCursor);
    }

    /**
     * Pretty much the same as the User statistics but no longer checks for the user being on the line read
     * Gets all max fishings to the map and shows the UI for it
     * @throws IOException
     */
    private void showGlobalStatistics() throws IOException {
        //Inicialize variables and Frame Container
        String line;
        int lineCursor = 0;
        HashMap <String, Double> maxFished = new HashMap<>();
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        frame.add(panel);
        frame.setSize(500, 500);
        centerFrameOnScreen(frame);
        //Reads all lines from the file and puts max weight of the fish
        do{
            lineCursor++;
            line = readLine(this.register, this.registerHashtags, lineCursor);
            double fishWeight = getFishWeight(line);
            String weightKey = getKeyByWeight(fishWeight, maxFished);
            if (fishWeight > maxFished.getOrDefault(weightKey, fishWeight)) {
                maxFished.put(weightKey, fishWeight);
            }
            else{
                maxFished.put(getFishNameOnRegister(line), fishWeight);
            }
        }while(!line.isEmpty());
        mapToLabel(maxFished, frame, panel, lineCursor);
    }

    /**
     * Parses the user choice as "name" to "name.txt"
     * @return the File
     */
    private File inputPesquera()  {
        String fisherName = "";
        int pesquera = JOptionPane.showOptionDialog(null, "On vols pescar?","Pesquera",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"florida", "mediterrania", "atlantico"}, "florida");

            switch (pesquera) {
                case 0:
                    fisherName = "florida";
                    break;
                case 1:
                    fisherName = "mediterrania";
                    break;
                case 2:
                    fisherName = "atlantico";
                    break;
                default:
                    break;
            }
        return new File(this.arxiusFolder, fisherName +".txt");
    }

    /**
     * Calls recursively the "hookAfish" method which generates a random value to fish
     * @Return the fish hooked
     */
    private String fishHooked(File fisher, String username) throws IOException {
        String fish;
        do{
            fish = hookAfish(fisher, username);
        }while (fish.isBlank());
        return fish;
    }

    // Centers the JFrame onto the screen
    private void centerFrameOnScreen(JFrame frame){
        Toolkit it=Toolkit.getDefaultToolkit();
        Dimension d=it.getScreenSize();
        int w=frame.getWidth(), h=frame.getHeight();
        frame.setLocation(d.width/2-w/2, d.height/2-h/2);
    }

    // Reads the subLine according to the fish weight and returns it parsed to Double
    private Double getFishWeight(String line) throws IOException {
        if(line.isEmpty()){
            return 0.0;
        }
        return Double.parseDouble(readSubLine(line, 1, 3));
    }


    // gets the String Key (fish name) of the map, if it exists. Otherwise empty string is treated
    private String getKeyByWeight(double weight, HashMap<String, Double> map){
        for(Map.Entry<String, Double> entry : map.entrySet()){
            if (entry.getValue().equals(weight)){
                return entry.getKey();
            }
        }
        return "";

    }
    // returns the sub line of the fish name on registers file
    private String getFishNameOnRegister(String line) throws IOException {
        return readSubLine(line, 1,2);
    }
    // returns the cursor of a line from the register given fish' name and it's weight
    private int getLineByNameAndWeight(String name, double weight) throws IOException {
        String line;
        String nameRead;
        double weightRead;
        int lineCursor = 0;
        do{
            lineCursor++;
            line = readLine(this.register, this.registerHashtags, lineCursor);
            nameRead = getFishNameFromLine(line);
            weightRead = getFishWeightFromLine(line);
            if(name.equals(nameRead) && weight == weightRead){
                return lineCursor;
            }
        }while(!line.isEmpty());
        return lineCursor;
    }


    // Creates an UI representation of the hashmap according to highest fishings
    private void mapToLabel(Map<String, Double> map, JFrame frame, JPanel panel, int lineCursor) throws IOException {
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            // we do not want to show empty/useless results
            if (entry.getKey().isEmpty() || 0.0 == entry.getValue()){
                continue;
            }
            String lineRead = readLine(this.register, this.registerHashtags, getLineByNameAndWeight(entry.getKey(),
                    entry.getValue()));
            JLabel label = new JLabel(lineRead);
            label.setHorizontalAlignment(JLabel.CENTER);
            panel.add(label);
        }
        // creates the layout of the panel and also the button to close it
        panel.setLayout(new GridLayout(lineCursor, 1, 2, 2));
        JButton button = new JButton("Aceptar" );
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized(frame){
                    frame.notify();
                    frame.setVisible(false);
                }
                frame.dispose();
            }
        });
        //syncronized ensures the frame can be closed to further break and return to the main menu
        button.setPreferredSize(new Dimension(10, 25));
        frame.add(button, BorderLayout.SOUTH);
        frame.setVisible(true);
        synchronized(frame){
            try{
                frame.wait();
            }
            catch(InterruptedException ex){ }
        }
    }

    private String getUsernameFromRegister(String line) throws IOException {
       return readSubLine(line, 1, 1);
    }

    /**
     * Generates a random double between 0 and 1 and looks on the current fisher whether any fish can be hooked
     * It will be called recursively until a fish is hooked
     * @param fisher where we're fishing
     * @param username the user who's fishing
     * @return the line which may represent the fishing
     * @throws IOException
     */
    private String hookAfish(File fisher, String username) throws IOException {

        int lineCursor = 0;
        String line;
        String parsedLine = "";
        do{
            //reads lines
            lineCursor++;
            double probability = Math.random();
            line = readLine(fisher, this.hashtagspesquera, lineCursor);
            //this subline stands for fish' probability to be hooked
            String subLine = readSubLine(line, 1, 2);

            double fishProbability = Double.parseDouble(subLine);
            //checks whether it's hooked or not and if it's the case parses the line indeed
            if (fishProbability >= probability){
                String fishName = getFishNameFromLine(line);
                double fishMinWeight = Double.parseDouble(readSubLine(line, 1, 3));
                double fishMaxWeight = Double.parseDouble(readSubLine(line, 1, 4));
                //generates the hooked fish' random weight
                double fishWeight = fishMinWeight + Math.random() * (fishMaxWeight - fishMinWeight);
                String parsedWeight = String.format( "%.3f", fishWeight).replace(",", ".");
                String date = getCurrentDate();
                JOptionPane.showMessageDialog(null,"Has pescat un " + fishName + " de " + parsedWeight + "Kg");
                return parseLine(username, fishName, parsedWeight, date, fisher.getName().replace(".txt", ""));
            }
        }while (!line.isBlank());
        return parsedLine;
    }


    private String getFishNameFromLine(String line) throws IOException {
        return  readSubLine(line, 1, 2);
    }
    private double getFishWeightFromLine(String line) throws IOException {
        return Double.parseDouble(readSubLine(line, 1, 3));
    }

    //gets the current date in catalan
    private String getCurrentDate(){
        Calendar calendar = new GregorianCalendar();
        return new SimpleDateFormat("dd MMMM, yyyy", new Locale("ca", "ES")).format(calendar.getTime());
    }
    //parses the line by adding a hashtag between every argument given --variable--
    String parseLine(String... args){
        String parsedLine = "#";
        for(String arg: args){
            for(char character: arg.toCharArray()){
                parsedLine += character;
            }
            parsedLine += '#';
        }
        return parsedLine;
    }
    //shows choices from the menu
    private String showChoices(){
        return  JOptionPane.showInputDialog(
                "***********************************************************\n*" +
                " Benvinguts al programa de pesca *\n* Menu principal * " +
                        "\n***********************************************************\n"
                + "1) Donar d'alta un usuari\n2) Donar de baixa un usuari\n3) Pescar en una pesquera\n" +
                "4) Estadistiques per usuari\n5) Estadistiques globals\ns) Sortir del programa" +
                "\n***********************************************************\n OPCIO ?");
    }
    private String inputData(String missatge){
        return JOptionPane.showInputDialog(missatge);
    }

    // If the file requested is empty the outputstream will rewrite it. Otherwise, it will just append to it
    private OutputStream checkEmptyFile(String line, File file) throws IOException {
        if (line.isBlank()){
            return new FileOutputStream(file, false);
        }
        else{
            OutputStream outputStream = new FileOutputStream(file, true);
            writeBlankLine(outputStream);
            return outputStream;
        }
    }

    // rewrites a line directly to a file
    private void rewriteLine(String linia, File file) throws IOException {
        String firstLine = readLine(file, this.hashtagsUsuaris, 1);
        OutputStream writer = checkEmptyFile(firstLine, file);
        if (linia.isBlank()){
            writer.close();
            return;
        }
        writeLine(linia, writer);
        writer.close();
    }

    //writes the byte representation of a blank line
    private void writeBlankLine(OutputStream outputStream) throws  IOException{
        outputStream.write(10);
    }

    private void writeHashtag(OutputStream outputStream) throws IOException {
        outputStream.write('#');
    }

    //writes a line character by character
    private void writeLine(String string, OutputStream outputStream) throws IOException {
        for (char letter: string.toCharArray()){
            outputStream.write(letter);
        }
    }

    // Reads a line given it's cursor, the file and the hashtags limit. It's intended to be used recursively
    private String readLine(File arxiu, int hashtagLimit, int lineCursor) throws IOException {
        InputStream inputStream = new FileInputStream(arxiu);
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1);
        int data = reader.read();
        int hashtagCounter = 0;
        String line = "";

        while (data != -1){

            char readChar = (char) data;

            if (readChar == '#'){
                hashtagCounter++;
            }

            line += readChar;
            data = reader.read();

            // here this means we're one line before our desired
            if(hashtagCounter==(hashtagLimit * lineCursor) - hashtagLimit){
                line = "";
                continue;
            }

            // arrived the limit per the cursor it means we have already read the desired line
            if(hashtagCounter==hashtagLimit * lineCursor){
                    inputStream.close();
                    return line;
            }
            //every line it will empty itself
            if (hashtagCounter % hashtagLimit == 0){
                    line = "";
            }
            }
        reader.close();
        return line;
    }

    // returns a subline representing the value we want to eventually retrieve from a whole file
    private String readSubLine(String line, int numberOfValuesToReturn, int valuePosition)  {
        int hashtagCounter = 0;
        String subLine = "";

        for (char character: line.toCharArray()){
            if(character == '#'){
                character = ' ';
                hashtagCounter++;
            }
            subLine += character;
            // if we have reached this it means we have read the subline needed
            if(hashtagCounter == numberOfValuesToReturn + valuePosition ){
                return subLine.trim();
            }
            //this will empty the subline one value before the desired one
            if(hashtagCounter == valuePosition - numberOfValuesToReturn ){
               subLine = "";
            }
        }
        return subLine.trim();
    }

    //checks on the file whether the user already exists
    private Boolean userAlreadyExists(String username) throws IOException {
        boolean exists = false;
        String line;
        int lineCursor = 0;
      do{
          lineCursor++;
          line = readLine(this.usuarisFile, this.hashtagsUsuaris, lineCursor);
          String subLine = readSubLine(line, 2, 0);
          if (subLine.equals(username)){
              return true;
          }

      } while (!line.isBlank());

      return exists;
    }

    private Boolean credentialsAreCorrect(String username, String password) throws IOException {
        String lineToFind = '#' + username + '#' + password + '#';
        String lineRead;
        int lineCursor = 0;

        do{
            lineCursor++;
            lineRead = readLine(this.usuarisFile, this.hashtagsUsuaris, lineCursor);
            if(lineRead.equals(lineToFind)){
                return true;
            }
        }while(!lineRead.isEmpty());
        return false;
    }

}

