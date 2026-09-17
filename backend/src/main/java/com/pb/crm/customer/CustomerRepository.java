package com.pb.crm.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>, RevisionRepository<Customer, Long, Integer> {

    Optional<Customer> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByDocument(String document);

    boolean existsByDocumentAndIdNot(String document, Long id);

    List<Customer> findAllByOrderByNameAsc();

    Page<Customer> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("""
            select c from Customer c
            where lower(c.name) like lower(concat('%', :term, '%'))
               or lower(c.email) like lower(concat('%', :term, '%'))
               or c.document = :term
            """)
    Page<Customer> search(@Param("term") String term, Pageable pageable);
}
