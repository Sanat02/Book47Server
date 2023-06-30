package kg.attractor.java;
import kg.attractor.java.lesson44.Lesson46Server;
import kg.attractor.java.lesson44.Lesson47Server;

import java.io.IOException;

public class Main {
    /*  Краткое пояснение:
    1.Были исправлены методы get books и return books.Теперь можно получать и возвращать определенные книги через query parameters.
      1.1 При приобретении книжки меняется state="given" и bookTaker="логин пользователя(email)" book/get
      1.2 При возвращении книга удаляется из списка currentBooks,и добвляется в список takenBooks(список книг который пользователь когда либо взял), book/return
        меняется state="vacant" и bookTaker="none"

     2. Book info. Отображается информация об определенной книге Learn more -нажмите чтобы прочитать инфо.
     3. В profile отображаются сведения об авторизованном  пользователе

    */
    public static void main(String[] args) {
        try {
            new Lesson47Server("localhost", 8187).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
