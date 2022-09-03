import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class SudokuSolver extends TelegramLongPollingBot implements Runnable{
    private static List<Client> clientList;

    @Override
    public String getBotUsername() {
        return "Here_you_enter_the_BotUsername_from_BotFather";
    }

    @Override
    public String getBotToken() {
        return "Here_you_enter_the_BotToken_from_BotFather";
    }

    @Override
    public void onRegister() {
        super.onRegister();
        System.out.println("hello");
        clientList = new ArrayList<Client>();
        loadClientList();
    }

    private void loadClientList() {
        try {
            File clients = new File("src/clients/Clients.txt");
            try {
                clients.createNewFile();
            } catch (IOException ex) {
            }
            FileReader fr = new FileReader("src/clients/Clients.txt");
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            int counter=0;
            while (line != null && !line.equals("")){
                String[] client = line.split("\\*");
                clientList.add(new Client(client[2],client[0],counter,client[1],client[3]));
                line = br.readLine();
                counter++;
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("no clients");
        } catch (IOException e) {
            System.out.println("no clients");
        }
    }

    @Override
    public void onUpdateReceived(Update update) {

        //recognize the client and update his last enter
        int clientNum = getClientFromUpdate(update);
        clientList.get(clientNum).updateLastEnter();

        //if the same user sends more than one message at the same time, the new message added to the updates Queue
        //and the running thread taking care for them, if a new message sent by a different client a new thread starts
        clientList.get(clientNum).addUpdate(update);
        if (!clientList.get(clientNum).getThread().isAlive()){
            clientList.get(clientNum).setThread(new Thread(this));
            clientList.get(clientNum).getThread().start();
        }

    }
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        int clientNumber = Integer.parseInt(threadName);
        Queue<Update> updates = clientList.get(clientNumber).getUpdate();

        //going through all the messages that the specific client have sent
        while (updates.peek()!=null) {
            //a new inner thread runs for each message in the queue
            Thread innerThread = new Thread(threadName){
                @Override
                public void run() {
                    super.run();
                    System.out.println("try...");

                    int clientNum = Integer.parseInt(Thread.currentThread().getName());
                    Client client = clientList.get(clientNum);
                    //the first update in the queue polled and removed from the queue
                    Update update = client.getUpdate().poll();
                    if (update.hasMessage())
                        System.out.println(update.getMessage().getText());
                    //change language
                    if (update.hasCallbackQuery()) {
                        if (update.getCallbackQuery().getData().equals("English")){
                            System.out.println("change to english");
                            clientList.get(clientNum).resetAllMessagesEnglish();
                        } else if (update.getCallbackQuery().getData().equals("עברית")) {
                            System.out.println("change to Hebrew");
                            clientList.get(clientNum).resetAllMessagesHebrew();
                        }
                        try {
                            execute(client.getStartMessage());
                        } catch (TelegramApiException e) {
                        }
                    }
                    //show the inline buttons for change language
                    else if (update.getMessage().isCommand()){
                        if (update.getMessage().getText().equals("/start") || update.getMessage().getText().equals("/language")){
                            if (client.getLanguage().equals("English"))
                                client.setChooseLan(new SendMessage(client.getChatId()
                                        ,"hi " + update.getMessage().getChat().getFirstName()+" please select a language:"));
                            else
                                client.setChooseLan(new SendMessage(client.getChatId()
                                        ,"שלום " + update.getMessage().getChat().getFirstName()+" בחר שפה"));
                            client.getChooseLan().setReplyMarkup(client.getLanguageMarkup());

                            System.out.println("welcome");
                            try {
                                execute(client.getChooseLan());
                                System.out.println("s");
                            } catch (TelegramApiException e) {

                            }
                        }
                    }
                    // photo was sent
                    else if (update.hasMessage() && update.getMessage().hasPhoto() && !clientList.get(clientNum).isAsked()) {
                        //client.setImageProcessing(new ImageProcessing());
                        clientList.get(clientNum).setFoundSudoku(false);
                        //get the best photo from the user
                        File file = getTheFileFromTelegram(update, client);
                        try {
                            execute(client.getWaitASec());
                            //looking for all the rectangles in the image and checking all of them if its sudoku
                            client.setAllRectanglesImages(client.getImageProcessing().fileToListOfImages(file));
                            checkingAllImages(update, clientNum);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    // the user have been asked what digit does he see
                    else if (update.hasMessage() && update.getMessage().hasText() && clientList.get(clientNum).isAsked()) {

                        if (isNumeric(update.getMessage().getText())) {
                            if (Integer.parseInt(update.getMessage().getText()) > 0
                                    && Integer.parseInt(update.getMessage().getText()) < 10) {
                                client.getSudoku()[client.getSquareAskIndex()] = Integer.parseInt(update.getMessage().getText()) + 10;
                                client.setAsked(false);
                                for (int i = 0; i < 81; i++) {
                                    if (client.getSudoku()[i] < 10)
                                        client.getSudoku()[i] = 0;
                                }
                                try {
                                    askTheUserWhatDigitDoesHeSee(update, clientNum);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                if (!client.isBrook()) {
                                    try {
                                        checkingIfRightAndAskIfNeeded(update, clientNum);
                                    } catch (TelegramApiException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            } else {
                                try {
                                    execute(client.getBetween1_9());
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } else {
                            try {
                                execute(client.getBetween1_9());
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    //sent an image in the middle of the asking process
                    else if (update.hasMessage() && update.getMessage().hasPhoto() && clientList.get(clientNum).isAsked()) {
                        try {
                            execute(client.getBetween1_9());
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    // number was sent in order to create a new sudoku
                    else if (update.hasMessage() && update.getMessage().hasText() && !clientList.get(clientNum).isAsked()) {
                        String m = update.getMessage().getText();
                        if (isNumeric(m)) {
                            if (Integer.parseInt(m) > 22
                                    && Integer.parseInt(m) < 41) {
                                SendPhoto photo1 = new SendPhoto();
                                photo1.setChatId(update.getMessage().getChatId().toString());
                                int[] sudoku2 = new int[81];
                                sudoku2[0] = 10;
                                while (sudoku2[0] == 10) {
                                    System.gc();
                                    sudoku2 = client.getGameLogic().createSudoku(Integer.parseInt(m) - 3);
                                }
                                File file = new File("src/photos/grid.jpg");
                                BufferedImage image;
                                try {
                                    image = ImageIO.read(file);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                client.setBG(new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR));


                                Graphics2D g2 = image.createGraphics();
                                g2.drawImage(image, 0, 0, null);
                                g2.setColor(Color.black);
                                Font font = new Font("Ariel", Font.TYPE1_FONT, (image.getWidth() / 18));
                                g2.setFont(font);

                                int counter = 0;
                                for (int i = 0; i < 9; i++) {
                                    for (int j = 0; j < 9; j++) {
                                        BufferedImage b = client.getBG();
                                        if (sudoku2[counter] != 0) {
                                            g2.drawString(String.valueOf(sudoku2[counter]), (int) ((b.getWidth() / 21) + (j * b.getWidth() / 9.43) + 5),
                                                    (int) ((b.getHeight() / 11) + (i * (b.getHeight() / 9.49))) + 5);
                                        }
                                        counter++;
                                    }
                                }
                                g2.dispose();

                                File file1 = new File("src/photos/CreatedSudoku.png");

                                try {
                                    ImageIO.write(image, "png", file1);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }


                                InputFile inputFile1 = new InputFile(file1);
                                inputFile1.setMedia(file1);
                                photo1.setPhoto(inputFile1);
                                client.setCreated(Integer.parseInt(m));
                                try {
                                    execute(photo1);
                                    execute(client.getCreated());
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    execute(client.getStartMessage());
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (clientNum == 0) {
                            mangerTasks(m, client);
                        } else {
                            try {
                                execute(client.getStartMessage());
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            };
            innerThread.start();
            try {
                //if the inner Thread run more than 1.5 minutes it gets stock and force to stop
                innerThread.join(90000);
                if (innerThread.isAlive()){
                    innerThread.stop();
                    clientList.get(clientNumber).setStoppedImage(-1);
                    clientList.get(clientNumber).setStopped(-1);
                    System.out.println("forced to stop");
                    execute(clientList.get(clientNumber).getStock());
                }else {
                    System.out.println("finished with this update");
                }

            } catch (InterruptedException e) {
            } catch (TelegramApiException e) {
            }
        }


    }
    private int getClientFromUpdate(Update update) {
        String chatId;
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId().toString();
        }
        else{
            chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
        }
        //if he is already in the list
        for (int i = 0; i < clientList.size(); i++) {
            if (clientList.get(i).getChatId().equals(chatId))
                return i;
        }
        //if not register him
        clientList.add(new Client(update.getMessage().getChatId().toString(),
                update.getMessage().getChat().getFirstName()+" "+update.getMessage().getChat().getLastName(),clientList.size()));


        return clientList.size()-1;
    }
    //return true if found sudoku in the list from the point he was before until the end
    private boolean checkingAllImages(Update update,int clientNum) throws IOException, TelegramApiException {

        Client client = clientList.get(clientNum);
        //try to check the last image in the list,(the big photo) just if you did not find another sudoku so far
        for (int i = client.getStoppedImage()+1; i < client.getAllRectanglesImages().size()-1
                || (i==client.getAllRectanglesImages().size()-1 && !client.isFoundSudoku()); i++) {
            resetTheVariables(update,client);
            client.setSudoku(client.getImageProcessing()
                            .sudokuArrayFromImage(client.getAllRectanglesImages().get(i)));
            client.setImg2(client.getAllRectanglesImages().get(i));
            if (client.getSudoku().length != 1) {
                //second sudoku was found
                if (client.isFoundSudoku()) {
                    execute(client.getAnotherSudoku());
                }
                client.setFoundSudoku(true);
                client.setBG(client.getImg2());
                int problematicSquare = 0;
                for (int k = 0; k < 81; k++) {
                    if (client.getSudoku()[k] < 10 && client.getImageProcessing().getDigitsInTheSquare()[k]) {
                        problematicSquare++;
                    }
                }
                client.setStoppedImage(i);
                if (problematicSquare < 15 && problematicSquare!=0) {
                    askTheUserWhatDigitDoesHeSee(update,clientNum);
                }
                if (!client.isAsked()) {
                    checkingIfRightAndAskIfNeeded(update,clientNum);
                }
                return true;
            }
        }
        if (!client.isFoundSudoku()){
            try {
                execute(client.getDidNotReadRight());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    private void checkingIfRightAndAskIfNeeded(Update update,int clientNum) throws TelegramApiException {
        Client client = clientList.get(clientNum);

        int[] givenNum = new int[81];
        for (int i = 0; i < 81; i++) {
            if (client.getSudoku()[i]>10){
                givenNum[i]=client.getSudoku()[i];
                client.getSudoku()[i]-=10;}
        }

        client.setSudoku(client.getGameLogic().getSolved(client.getSudoku()));
        client.setReadWrong(false);
        for (int i = 0; i < 81; i++) {
            if (client.getSudoku()[i]==0){
                client.setReadWrong(true);
                client.setSudoku(client.getImageProcessing()
                        .findAllContradictionsAndRemoveThem(client.getSudoku()));
                client.setStopped(-1);
                for (int j = 0; j < 81; j++) {
                    if (client.getSudoku()[j]!=0)
                        client.getSudoku()[j]+=10;
                }
                try {
                    askTheUserWhatDigitDoesHeSee(update,clientNum);
                    if(!client.isBrook()){
                        client.setReadWrong(true);
                        client.setSudoku(client.getImageProcessing()
                                .removeAllNumThatWithOutThemThereIsSolution(client.getSudoku(),client.isNoSolution()));
                        client.setStopped(-1);
                        for (int j = 0; j < 81; j++) {
                            if (client.getSudoku()[j]!=0)
                                client.getSudoku()[j]+=10;
                        }
                        if (!client.isNoSolution()){
                            askTheUserWhatDigitDoesHeSee(update,clientNum);
                        }
                        else {
                            execute(client.getErrorInTheSudoku());
                            client.setStoppedImage(i);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
        //read right
        if (!client.isReadWrong()){
            int sol = countSolutions(client);
            if(sol >= 1) {
                if (sol==2) {
                    execute(client.getTwoSolutions());
                }

                for (int i = 0; i < 81; i++) {
                    if (givenNum[i] != 0)
                        client.getSudoku()[i] = 0;
                }
                try {
                    sendTheSolution(client);
                    //maybe more sudokus in the photo
                    if (client.getStoppedImage() < client.getAllRectanglesImages().size() - 2
                            && client.getStoppedImage() > -1
                            || (client.getStoppedImage() == client.getAllRectanglesImages().size() - 2
                            && !client.isFoundSudoku())) {
                        if (!checkingAllImages(update,clientNum)) {
                            execute(client.getStartMessage());
                            client.setStoppedImage(-1);
                        }
                    } else {
                        execute(client.getStartMessage());
                        client.setStoppedImage(-1);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (sol == 0) {
                execute(client.getErrorInTheSudoku());
            }
        }

        else if (!client.isBrook()){
            execute(client.getErrorInTheSudoku());
            client.setStoppedImage(-1);
            client.setAsked(false);
            client.setSquareAskIndex(0);
        }

    }
    private int countSolutions(Client client) {

        int[] onlyGivenNumbers = new int[81];
        for (int i = 0; i < 81; i++) {
            if (client.getImageProcessing().getDigitsInTheSquare()[i]){
                onlyGivenNumbers[i]=client.getSudoku()[i];
            }
        }
        return (client.getGameLogic().countSolutions(onlyGivenNumbers));
    }

    private File getTheFileFromTelegram(Update update, Client client) {
        PhotoSize photoSize=update.getMessage().getPhoto().get(0);
        for (int i = 1; i < update.getMessage().getPhoto().size(); i++) {
            if (update.getMessage().getPhoto().get(i).getHeight()>photoSize.getHeight()){
                photoSize=update.getMessage().getPhoto().get(i);
            }
        }
        GetFile getFile = new GetFile(photoSize.getFileId());
        File file;
        try {
            String path = this.execute(getFile).getFilePath();
            URL url = new URL("https://api.telegram.org/file/bot" + getBotToken() + "/" + path);
            client.setImg2(ImageIO.read(url));
            file = new File("src/photos/downloaded.jpg");
            ImageIO.write(client.getImg2(), "jpg", file);

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }
    private void resetTheVariables(Update update, Client client) {
        client.getGameLogic().setSudoku(new int[81]);
        client.getGameLogic().setSolved(new int[81]);
        client.getImageProcessing().setDigitCounter(0);
        client.getImageProcessing().setEightyOneDigitsRec(new ArrayList<Rectangle>());

        client.setSudoku(new int[81]);
        client.setSquareAskIndex(0);
        client.setStopped(-1);
        client.setReadWrong(false);
        client.setBrook(false);
        client.setAsked(false);
        client.setPhoto(new SendPhoto());
        client.getPhoto().setChatId(update.getMessage().getChatId().toString());
        client.getPhoto().setReplyMarkup(client.getAskedRemove());
        client.setNoSolution(false);
    }
    private void askTheUserWhatDigitDoesHeSee(Update update,int clientNum) throws IOException {
        Client client = clientList.get(clientNum);
        List<KeyboardRow> Buttons = new ArrayList<>();
        int counter=1;
        for (int i = 0; i < 3; i++) {

            KeyboardRow buttonList = new KeyboardRow();
            for (int j = 0; j < 3; j++) {
                buttonList.add(new KeyboardButton(String.valueOf(counter++)));
            }
            Buttons.add(buttonList);
        }
        client.getAskMarkup().setKeyboard(Buttons);
        client.getQuestionMessage().setReplyMarkup(client.getAskMarkup());

        SendPhoto questionPhoto = new SendPhoto();
        questionPhoto.setChatId(update.getMessage().getChatId().toString());

        client.setBrook(false);
        for (int i = clientList.get(clientNum).getStopped()+1; i < 81; i++) {
            if (client.getSudoku()[i] < 10 && client.getImageProcessing().getDigitsInTheSquare()[i]) {
                File askFile = new File("src/photos/digit" + i + ".jpg");
                BufferedImage askImage = client.getImageProcessing().writeImageToFile("src/photos/digit" + i,
                        client.getImageProcessing().getSquare(i));
                ImageIO.write(askImage, "jpg", askFile);
                InputFile ask = new InputFile(askFile);
                ask.setMedia(askFile);
                questionPhoto.setPhoto(ask);
                try {
                    execute(questionPhoto);
                    execute(client.getQuestionMessage());
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                client.setAsked(true);
                client.setSquareAskIndex(i);
                client.setStopped(i);
            }
            if (client.isAsked()){
                client.setStopped(i);
                client.setBrook(true);
                break;
            }
        }
    }
    private void sendTheSolution(Client client) throws IOException, TelegramApiException {
        Graphics2D g2 = client.getImg2().createGraphics();
        Font font = new Font("Ariel", Font.TYPE1_FONT, (client.getBG().getWidth() / 18));
        g2.setFont(font);

        //corners[0] = bottom right
        //corners[1] = bottom left
        //corners[2] = upper right
        //corners[3] = upper left
        int[] corners = client.getImageProcessing().getCorners();
        double tLine = corners[2] - corners[3];
        double bLine = corners[0] - corners[1];
        double leftGap = corners[1] - corners[3];
        double relativeLeftGap;
        double relativeWidth;


        int counter = 0;
        for (int i = 0; i < 9; i++) {
            //the top line is longer
            if (tLine > bLine) {
                relativeWidth = ((1. / 9. * (9 - (double) i)) * tLine) + ((1. / 9. * i) * bLine);
                relativeLeftGap = ((1. / 9. * i) * leftGap);
            }
            //the bottom line is longer
            else {
                relativeWidth = ((1. / 9. * (9 - (double) i)) * bLine) + ((1. / 9. * i) * tLine);
                relativeLeftGap = ((1. / 9. * (9 - (double) i)) * leftGap);
            }

            for (int j = 0; j < 9; j++) {
                if (client.getSudoku()[counter] < 10 && client.getSudoku()[counter] > 0) {
                    g2.setColor(Color.red);
                    g2.drawString(String.valueOf(client.getSudoku()[counter]),
                            (int) (Math.min(corners[3], corners[1]) + relativeLeftGap + (client.getBG().getWidth() / 21) + (j * relativeWidth / 9)),
                            client.getImageProcessing().getYCoordinate() + (client.getBG().getHeight() / 11) + (i * (client.getBG().getHeight() / 9)));
                }
                counter++;
            }
        }
        g2.dispose();

        File file1 = new File("src/photos/SolvedSudoku.jpg");

        ImageIO.write(client.getImg2(), "jpg", file1);


        InputFile inputFile1 = new InputFile(file1);
        inputFile1.setMedia(file1);
        client.getPhoto().setPhoto(inputFile1);

        try {
            execute(client.getPhoto());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void mangerTasks(String m,Client client) {
        //manager ask for clients data
        if (m.equals("a")){
            client.getClientMessage().setText("יש לך "+String.valueOf(clientList.size()-1)+" משתמשים");
            try {
                execute(client.getClientMessage());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else if (m.equals("b")) {
            for (int i = 1; i < clientList.size(); i++) {
                client.getClientMessage().setText(clientList.get(i).toMessage());
                try {
                    execute(client.getClientMessage());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (m.contains(" ")) {
            boolean foundUser = false;
            for (int i = 0; i < clientList.size(); i++) {
                if (m.equals(clientList.get(i).getName())){
                    client.getClientMessage().setText(clientList.get(i).toMessage());
                    try {
                        execute(client.getClientMessage());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    foundUser = true;
                    break;
                }
            }
            if (!foundUser){
                try {
                    execute(client.getStartMessage());
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                execute(client.getStartMessage());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
    public static boolean isNumeric(String string) {
        int intValue;

        System.out.println(String.format("Parsing string: \"%s\"", string));

        if(string == null || string.equals("")) {
            System.out.println("String cannot be parsed, it is null or empty.");
            return false;
        }

        try {
            intValue = Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Input String cannot be parsed to Integer.");
        }
        return false;
    }


}
