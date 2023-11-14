package com.example.hibernateinclausememoryleak;

import com.example.hibernateinclausememoryleak.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    @Query("""
    SELECT b
    FROM Book b
    WHERE b.id in :ids
    """)
    Collection<Book> getBooksById(Collection<Integer> ids);
}
