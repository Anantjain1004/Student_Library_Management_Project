package com.example.Student_Library_Management_System.Services;

import com.example.Student_Library_Management_System.DTOs.IssueBookRequestDto;
import com.example.Student_Library_Management_System.DTOs.ReturnBookRequestDto;
import com.example.Student_Library_Management_System.Enums.CardStatus;
import com.example.Student_Library_Management_System.Enums.TransactionStatus;
import com.example.Student_Library_Management_System.Model.Book;
import com.example.Student_Library_Management_System.Model.Card;
import com.example.Student_Library_Management_System.Model.Transactions;
import com.example.Student_Library_Management_System.Repositories.BookRepository;
import com.example.Student_Library_Management_System.Repositories.CardRepository;
import com.example.Student_Library_Management_System.Repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    CardRepository cardRepository;
    public String issueBook(IssueBookRequestDto issueBookRequestDto) throws Exception{

        int bookId = issueBookRequestDto.getBookId();
        int cardId = issueBookRequestDto.getCardId();


        //Get the book and card Entity
        //we are doing this bcz we want to the transaction attributes
        Book book = bookRepository.findById(bookId).get();
        Card card = cardRepository.findById(cardId).get();

        //final goal is to make transaction Entity, set its attribute
        //and save it

        Transactions transaction = new Transactions();
        //setting the attributes
        transaction.setBook(book);
        transaction.setCard(card);
//        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setIssueOperation(true);
        transaction.setTransactionStatus(TransactionStatus.PENDING);

        //attribute left is success/Failure
        //Check for validations
        if(book == null || book.isIssued() == true){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);//because we have to record Failed transaction
            throw new Exception("Book is not available");
        }

        if (card == null || (card.getCardStatus() != CardStatus.ACTIVATED)){
            transactionRepository.save(transaction);//because we have to record Failed transaction
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            throw new Exception("Card is not valid");
        }

        //We have reached a success case
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);

        //set the attributes of book
        book.setIssued(true);
        List<Transactions> listOfTransactionForBook = book.getListOfTransactions();
        listOfTransactionForBook.add(transaction);

        //I need to make changes in card
        //Book and the card
        List<Book> issuedBooksForCard = card.getBooksIssued();
        issuedBooksForCard.add(book);
        card.setBooksIssued(issuedBooksForCard);


        //Card and the Transaction: bidirectional (parent class
        List<Transactions> transactionListForCard = card.getTransactionsList();
        transactionListForCard.add(transaction);
        card.setTransactionsList(transactionListForCard);


        //save the parent
        cardRepository.save(card);
        //automatically, book and card transaction will be saved.
        //saving the parent


        return "Book issued successfully.";
    }

    public String getTransactions(int bookId,int cardId){
        List<Transactions> transactionsList = transactionRepository.getTransactionsForBookAndCard(bookId,cardId);
        String transactionId = transactionsList.get(0).getTransactionId();
        return transactionId;
    }

    public String returnBook(ReturnBookRequestDto returnBookRequestDto) throws Exception{
        int bookId = returnBookRequestDto.getBookId();
        int cardId = returnBookRequestDto.getCardId();
        Book book = bookRepository.findById(bookId).get();
        Card card = cardRepository.findById(cardId).get();

        Transactions transaction = new Transactions();

        if(book == null || book.isIssued() == false){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);//because we have to record Failed transaction
            throw new Exception("Book is not issued");
        }

        if (card == null || (card.getCardStatus() != CardStatus.ACTIVATED)){
            transactionRepository.save(transaction);//because we have to record Failed transaction
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            throw new Exception("Card is not valid");
        }

        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        book.setIssued(false);
        cardRepository.delete(card);
        return "Book returned successfully";
    }


}
