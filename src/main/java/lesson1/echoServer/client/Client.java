package lesson1.echoServer.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;


/**
 * Кастомный Swing клиент для эхо сервера
 *
 * Client commands: upload  filename - выгрузка файла по имени на сервер
 * download - загрузка файла по имени с сервера
 */
public class Client extends JFrame {

    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;


    public Client()  throws IOException {

        //инициализация
        socket = new Socket("localhost",6789);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        //создаем формочку
        setSize(300, 300);
        JPanel panel = new JPanel(new GridLayout(2,1));

        JTextField textField = new JTextField();
        JButton bntSend = new JButton("Send");

        bntSend.addActionListener( ae -> {
            var cmds = textField.getText().split(" ");

            //since java13 modern switch

            switch(cmds[0].toLowerCase()){
                case "upload" -> sendFile(cmds[1]);
                case "download" ->   getFile(cmds[1]);

                default -> donothing();
            }



            var message = textField.getText();

//            sendMessage(message);
        });

        addWindowListener(new WindowAdapter() {
            /**
             * Invoked when a window is in the process of being closed.
             * The close operation can be overridden at this point.
             *
             * @param e
             */
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                sendMessage("exit");
            }
        });



        panel.add(textField);
        panel.add(bntSend);

        add(panel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }


    private void donothing() {
        System.out.println("do nothing method");
        //just a stub
    }

    private void getFile(String filename)  {
        // todo: домашнее задание  13.05.2021
        System.out.println("get file mode");
        try {
            out.writeUTF("download");

            File file = new File("client/" + filename);
            if (!file.exists()){
                System.out.println("Создаю файл "+filename);
              var success = file.createNewFile();
              if (success){
                  System.out.println("Файл удалось сгенерировать");
              }else{
                  System.out.println("Файл не удалось сгенерировать");
              }
            }
            System.out.println("тут проблема");
            FileOutputStream fos = new FileOutputStream(file);
            var size = in.readLong();
            byte[] buffer = new byte[8*1024];
            var upperLimit = (size+buffer.length)/ buffer.length;
            for (int i = 0; i < upperLimit; i++) {
                var read = in.read(buffer);
                fos.write(buffer, 0, read);
            }

            fos.close();
            System.out.println("закрыл поток");
            out.writeUTF("DOWNLOAD. OK");

        } catch (IOException e) {
           // out.writeUTF("DOWNLOAD. WRONG");
            e.printStackTrace();
        }


    }

    /**
     * протокол передачи
     * в Out пересылается тег команды (upload) имя файла(1.txt), размер в байтах, 
     * сам файл фрагментами по 8кб
     * @param filename
     */
    private void sendFile(String filename) {
        try {
            System.out.println("send file mode");
            File file = new File("client/" + filename);
            if (!file.exists()) {
                System.out.println("File not exists");
                throw new FileNotFoundException();
            }

            long fileLength = file.length();
            FileInputStream fis = new FileInputStream(file);


            out.writeUTF("upload"); //send command
            out.writeUTF(filename);  //send filename
            out.writeLong(fileLength); //send fileLength

            int read = 0;
            byte[] buffer = new byte[8 * 1024];
            while ((read = fis.read(buffer)) != -1){
                out.write(buffer, 0 , read);

            }
            out.flush();
            System.out.println("файл загружен жду ответа сервера");
            var status = in.readUTF();
            System.out.println("Sending status: "+status);


        } catch (FileNotFoundException e) {
         System.err.println("File not found - /client/" + filename);
         e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Метод отправки сообщений на сервер
     * @param message строка сообщения
     */
    private void sendMessage(String message) {
        try {
            out.writeUTF(message);
            String command = in.readUTF();
//            if ("done".equalsIgnoreCase(command)){
//                System.out.println(command);
//            }
            System.out.println(command);
        } catch (EOFException eofException){
            System.err.println("Reading command error from "+socket.getInetAddress());
        }

        catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) throws IOException {
        new Client();
    }
}
