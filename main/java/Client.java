import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Client {
    private boolean asked;
    private int stopped;
    private int stoppedImage;
    private int num;
    private Thread thread;
    private Queue<Update> update;
    private String chatId;
    private String name;
    private String lastEnter;
    private String language;
    private  int[] sudoku = new int[81];
    private  int squareAskIndex = 0;
    private  List<BufferedImage> allRectanglesImages;
    private  boolean foundSudoku = false;
    private  BufferedImage img2;
    private  BufferedImage BG;
    private  SendPhoto photo;
    private  boolean Brook=false;
    private  boolean readWrong=false;
    private  boolean noSolution = false;
    private ImageProcessing imageProcessing;
    private GameLogic gameLogic;
    private  SendMessage waitASec = new SendMessage();
    private  SendMessage didNotReadRight = new SendMessage();
    private  SendMessage anotherSudoku = new SendMessage();
    private  SendMessage startMessage = new SendMessage();
    private  SendMessage questionMessage = new SendMessage();
    private  SendMessage errorInTheSudoku = new SendMessage();
    private  SendMessage twoSolutions = new SendMessage();
    private  SendMessage between1_9 = new SendMessage();
    private  SendMessage clientMessage = new SendMessage();
    private  SendMessage stock = new SendMessage();
    private  SendMessage chooseLan = new SendMessage();
    private  SendMessage created = new SendMessage();

    private  ReplyKeyboardRemove askedRemove = new ReplyKeyboardRemove(true);
    private  ReplyKeyboardMarkup askMarkup = new ReplyKeyboardMarkup();
    private ReplyKeyboardRemove languageRemove = new ReplyKeyboardRemove(true);

    private InlineKeyboardMarkup languageMarkup = new InlineKeyboardMarkup ();
    public Client(String chatId,String name,int num) {
        this.asked=false;
        this.stopped = -1;
        this.stoppedImage = -1;
        this.chatId = chatId;
        this.name = name;
        this.num = num;
        this.thread = new Thread(String.valueOf(num));
        this.update = new LinkedList<Update>();
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        this.lastEnter = localDateTime.format(dateTimeFormatter);
        this.gameLogic = new GameLogic();
        this.imageProcessing = new ImageProcessing(gameLogic);
        this.language = "English";
        resetAllMessagesEnglish();

    }
    public Client(String chatId,String name,int num,String lastEnter,String language) {
        this.asked=false;
        this.stopped = -1;
        this.stoppedImage = -1;
        this.chatId = chatId;
        this.name = name;
        this.num = num;
        this.thread = new Thread(String.valueOf(num));
        this.update = new LinkedList<Update>();

        this.lastEnter = lastEnter;
        this.gameLogic = new GameLogic();
        this.imageProcessing = new ImageProcessing(gameLogic);
        this.language = language;
        if (language.equals("Hebrew")) {
            resetAllMessagesHebrew();
        }
        else {
            resetAllMessagesEnglish();
        }

    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isAsked() {
        return asked;
    }

    public void setAsked(boolean asked) {
        this.asked = asked;
    }

    public int getStopped() {
        return stopped;
    }

    public void setStopped(int stopped) {
        this.stopped = stopped;
    }

    public int getStoppedImage() {
        return stoppedImage;
    }

    public void setStoppedImage(int stoppedImage) {
        this.stoppedImage = stoppedImage;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastEnter() {
        return lastEnter;
    }

    public void setLastEnter(String lastEnter) {
        this.lastEnter = lastEnter;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
        this.thread.setName(String.valueOf(num));
    }

    public ReplyKeyboardRemove getLanguageRemove() {
        return languageRemove;
    }

    public void setLanguageRemove(ReplyKeyboardRemove languageRemove) {
        this.languageRemove = languageRemove;
    }

    public InlineKeyboardMarkup getLanguageMarkup() {
        return languageMarkup;
    }

    public void setLanguageMarkup(InlineKeyboardMarkup languageMarkup) {
        this.languageMarkup = languageMarkup;
    }

    public Queue<Update> getUpdate() {
        return update;
    }

    public void addUpdate(Update update) {
        this.update.add(update);
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int[] getSudoku() {
        return sudoku;
    }

    public void setSudoku(int[] sudoku) {
        this.sudoku = sudoku;
    }

    public int getSquareAskIndex() {
        return squareAskIndex;
    }

    public void setSquareAskIndex(int squareAskIndex) {
        this.squareAskIndex = squareAskIndex;
    }

    public List<BufferedImage> getAllRectanglesImages() {
        return allRectanglesImages;
    }

    public void setAllRectanglesImages(List<BufferedImage> allRectanglesImages) {
        this.allRectanglesImages = allRectanglesImages;
    }

    public boolean isFoundSudoku() {
        return foundSudoku;
    }

    public void setFoundSudoku(boolean foundSudoku) {
        this.foundSudoku = foundSudoku;
    }

    public BufferedImage getImg2() {
        return img2;
    }

    public void setImg2(BufferedImage img2) {
        this.img2 = img2;
    }

    public BufferedImage getBG() {
        return BG;
    }

    public void setBG(BufferedImage BG) {
        this.BG = BG;
    }

    public SendPhoto getPhoto() {
        return photo;
    }

    public void setPhoto(SendPhoto photo) {
        this.photo = photo;
    }

    public boolean isBrook() {
        return Brook;
    }

    public void setBrook(boolean brook) {
        Brook = brook;
    }

    public boolean isReadWrong() {
        return readWrong;
    }

    public void setReadWrong(boolean readWrong) {
        this.readWrong = readWrong;
    }

    public boolean isNoSolution() {
        return noSolution;
    }

    public void setNoSolution(boolean noSolution) {
        this.noSolution = noSolution;
    }

    public ImageProcessing getImageProcessing() {
        return imageProcessing;
    }

    public void setImageProcessing(ImageProcessing imageProcessing) {
        this.imageProcessing = imageProcessing;
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }

    public void setGameLogic(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    public SendMessage getWaitASec() {
        return waitASec;
    }

    public void setWaitASec(SendMessage waitASec) {
        this.waitASec = waitASec;
    }

    public SendMessage getDidNotReadRight() {
        return didNotReadRight;
    }

    public void setDidNotReadRight(SendMessage didNotReadRight) {
        this.didNotReadRight = didNotReadRight;
    }

    public SendMessage getAnotherSudoku() {
        return anotherSudoku;
    }

    public void setAnotherSudoku(SendMessage anotherSudoku) {
        this.anotherSudoku = anotherSudoku;
    }

    public SendMessage getStartMessage() {
        return startMessage;
    }

    public void setStartMessage(SendMessage startMessage) {
        this.startMessage = startMessage;
    }

    public SendMessage getQuestionMessage() {
        return questionMessage;
    }

    public void setQuestionMessage(SendMessage questionMessage) {
        this.questionMessage = questionMessage;
    }

    public SendMessage getErrorInTheSudoku() {
        return errorInTheSudoku;
    }

    public void setErrorInTheSudoku(SendMessage errorInTheSudoku) {
        this.errorInTheSudoku = errorInTheSudoku;
    }

    public SendMessage getTwoSolutions() {
        return twoSolutions;
    }

    public void setTwoSolutions(SendMessage twoSolutions) {
        this.twoSolutions = twoSolutions;
    }

    public SendMessage getBetween1_9() {
        return between1_9;
    }

    public void setBetween1_9(SendMessage between1_9) {
        this.between1_9 = between1_9;
    }

    public SendMessage getClientMessage() {
        return clientMessage;
    }

    public void setClientMessage(SendMessage clientMessage) {
        this.clientMessage = clientMessage;
    }

    public SendMessage getStock() {
        return stock;
    }

    public void setStock(SendMessage stock) {
        this.stock = stock;
    }

    public ReplyKeyboardRemove getAskedRemove() {
        return askedRemove;
    }

    public SendMessage getCreated() {
        return created;
    }

    public void setCreated(int created) {
        if (language.equals("Hebrew"))
            this.created.setText("לסודוקו הזה יש בדיוק "+created+" מספרים על הלוח, ויש לו רק פתרון אחד");
        else
            this.created.setText("This Sudoku contains exactly "+created+" numbers on the board, and it has just one solution");
        this.created.setChatId(chatId);
    }

    public void setAskedRemove(ReplyKeyboardRemove askedRemove) {
        this.askedRemove = askedRemove;
    }

    public ReplyKeyboardMarkup getAskMarkup() {
        return askMarkup;
    }

    public void setAskMarkup(ReplyKeyboardMarkup askMarkup) {
        this.askMarkup = askMarkup;
    }
    public void resetAllMessagesHebrew() {
        waitASec.setChatId(chatId);
        waitASec.setText("חכה דקה מנסה לפתור לך את הסודוקו");
        didNotReadRight.setChatId(chatId);
        didNotReadRight.setText("הבוט לא מצא את הסדוקו בתמונה... אתה בטוח ששלחת תמונה של סדוקו?");

        startMessage.setChatId(chatId);
        startMessage.setText("תצלם לי תמונה של סדוקו ואנסה לפתור לך אותו, או שפשוט תשלח לי מספר בין 23 ל40 כדי לייצר סדוקו חדש, המספר מציין את כמות המספרים בסודוקו");

        anotherSudoku.setChatId(chatId);
        anotherSudoku.setText("מצאתי עוד סדוקו בתמונה");
        errorInTheSudoku.setChatId(chatId);
        errorInTheSudoku.setText("אתה עושה לי קטע אה? אין פתרון לסודוקו הזה...");
        twoSolutions.setChatId(chatId);
        twoSolutions.setText("לסודוקו ששלחת יש יותר מפתרון אחד");
        between1_9.setChatId(chatId);
        between1_9.setText("יש להזין ספרה בין 1 ל9");
        clientMessage.setChatId(chatId);
        stock.setChatId(chatId);
        stock.setText("סליחה הבוט נתקע ולכן הבקשה שלך נמחקה... בבקשה תשלח שוב");


        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton a = new InlineKeyboardButton("English");
        InlineKeyboardButton b = new InlineKeyboardButton("עברית");

        a.setCallbackData("English");
        b.setCallbackData("עברית");
        rowInline.add(a);
        rowInline.add(b);
        rowsInline.add(rowInline);
        languageMarkup.setKeyboard(rowsInline);
        chooseLan.setReplyMarkup(languageMarkup);

        askedRemove.setSelective(true);
        errorInTheSudoku.setReplyMarkup(askedRemove);

        questionMessage.setChatId(chatId);
        questionMessage.setText("מה הספרה שמופיעה בתמונה?");
        questionMessage.setParseMode(ParseMode.MARKDOWN);
        questionMessage.enableMarkdown(true);
        askMarkup.setSelective(true);
        askMarkup.setResizeKeyboard(true);
        askMarkup.setOneTimeKeyboard(false);
        this.language = "Hebrew";

        try {
            Path path = Paths.get("src/clients/Clients.txt");
            List<String> fileContent = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
            if (num>=fileContent.size())
                fileContent.add("");
            fileContent.set(num, this.toString());
            Files.write(path, fileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("did not find the clients");
        }

    }
    public void resetAllMessagesEnglish() {
        waitASec.setChatId(chatId);
        waitASec.setText("Wait a sec... trying to solve your Sudoku");
        didNotReadRight.setChatId(chatId);
        didNotReadRight.setText("The bot couldn't find the Sudoku in the picture... are you sure it was a Sudoku?");
        startMessage.setChatId(chatId);
        startMessage.setText("Choose a number between 23 to 40, in order to create a new sudoku." +
                " the number is how many numbers will be in the Sudoku. or just take a photo of a any Sudoku and i'll try to solve it");
        anotherSudoku.setChatId(chatId);
        anotherSudoku.setText("I've found more than one Sudoku in the photo");
        errorInTheSudoku.setChatId(chatId);
        errorInTheSudoku.setText("I'm pretty sure there is no solution to this one");
        twoSolutions.setChatId(chatId);
        twoSolutions.setText("For this Sudoku there is more than one solution");
        between1_9.setChatId(chatId);
        between1_9.setText("Enter a digit between 1 to 9");
        clientMessage.setChatId(chatId);
        stock.setChatId(chatId);
        stock.setText("Ok, bad things happen sometimes... I failed to solve this one. Please try again");

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton a = new InlineKeyboardButton("English");
        InlineKeyboardButton b = new InlineKeyboardButton("עברית");

        a.setCallbackData("English");
        b.setCallbackData("עברית");
        rowInline.add(a);
        rowInline.add(b);
        rowsInline.add(rowInline);
        languageMarkup.setKeyboard(rowsInline);
        chooseLan.setReplyMarkup(languageMarkup);

        askedRemove.setSelective(true);
        errorInTheSudoku.setReplyMarkup(askedRemove);

        questionMessage.setChatId(chatId);
        questionMessage.setText("What number do you see here?");
        questionMessage.setParseMode(ParseMode.MARKDOWN);
        questionMessage.enableMarkdown(true);
        askMarkup.setSelective(true);
        askMarkup.setResizeKeyboard(true);
        askMarkup.setOneTimeKeyboard(false);
        this.language = "English";
        try {
            Path path = Paths.get("src/clients/Clients.txt");
            List<String> fileContent = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
            if (num>=fileContent.size())
                fileContent.add("");
            fileContent.set(num, this.toString());
            Files.write(path, fileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("did not find the clients");
        }

    }
    @Override
    public String toString() {
        String str=name+"*"+lastEnter+"*"+chatId+"*"+language;
        str = str.replace("null","");

        return str;
    }
    public String toMessage() {
        name = name.replace("null","");
        return name+" \nנכנס בפעם אחרונה: "+lastEnter;
    }
    public void updateLastEnter() {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        this.lastEnter = localDateTime.format(dateTimeFormatter);
        try {
            Path path = Paths.get("src/clients/Clients.txt");
            List<String> fileContent = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
            fileContent.set(num, this.toString());
            Files.write(path, fileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("did not find the clients");
        }

    }

    public SendMessage getChooseLan() {
        return chooseLan;
    }

    public void setChooseLan(SendMessage chooseLan) {
        this.chooseLan = chooseLan;
    }
}
