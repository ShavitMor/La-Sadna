package com.sadna.sadnamarket.domain.stores;

import com.sadna.sadnamarket.HibernateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class StoreRepositoryTest {

    //private IStoreRepository repo;

    @BeforeEach
    public void setUp() {
        //repo = new MemoryStoreRepository();
        IStoreRepository.cleanDB();
    }

    static Stream<IStoreRepository> repositoryProvider() {
        return Stream.of(new MemoryStoreRepository(), new HibernateStoreRepository());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void findStoreByID(IStoreRepository repo)  {
        int id0 = repo.addStore("Alice", "American Eagle",  "Beer Sheva", "Eagle@gmail.com", "0548970173");
        int id1 = repo.addStore("Bob", "Shufersal",  "Beer Sheva", "Shufersal@gmail.com", "0548970173");
        int id2 = repo.addStore("Netta", "H&m",  "Beer Sheva", "Hm@gmail.com", "0548970173");

        Store expected0 = new Store(id0, "Alice", new StoreInfo("American Eagle", "Beer Sheva", "Eagle@gmail.com", "0548970173"));
        Store expected1 = new Store(id1, "Bob", new StoreInfo("Shufersal",  "Beer Sheva", "Shufersal@gmail.com", "0548970173"));
        Store expected2 = new Store(id2, "Netta", new StoreInfo("H&m",  "Beer Sheva", "Hm@gmail.com", "0548970173"));

        assertEquals(expected0, repo.findStoreByID(id0));
        assertEquals(expected1, repo.findStoreByID(id1));
        assertEquals(expected2, repo.findStoreByID(id2));
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getAllStoreIds(IStoreRepository repo)  {
        Set<Integer> expected0 = new HashSet<>();
        Set<Integer> expected1 = new HashSet<>();
        Set<Integer> expected2 = new HashSet<>();
        Set<Integer> expected3 = new HashSet<>();

        assertEquals(expected0, new HashSet<>(repo.getAllStoreIds()));

        int id0 = repo.addStore("Alice", "American Eagle", "Beer Sheva", "Eagle@gmail.com", "0548970173");
        Collections.addAll(expected1, id0);
        assertEquals(expected1, new HashSet<>(repo.getAllStoreIds()));

        int id1 = repo.addStore("Bob", "Shufersal",  "Beer Sheva", "Shufersal@gmail.com", "0548970173");
        Collections.addAll(expected2, id0, id1);
        assertEquals(expected2, new HashSet<>(repo.getAllStoreIds()));

        int id2 = repo.addStore("Netta", "H&m",  "Beer Sheva", "Hm@gmail.com", "0548970173");
        Collections.addAll(expected3, id0, id1, id2);
        assertEquals(expected3, new HashSet<>(repo.getAllStoreIds()));
    }

    /*@Test
    void deleteStore() {
        repo.addStore("Alice", "American Eagle",  "Beer Sheva", "Eagle@gmail.com", "0548970173");
        repo.addStore("Bob", "Shufersal",  "Beer Sheva", "Shufersal@gmail.com", "0548970173");

        assertTrue(repo.findStoreByID(0).getIsActive());
        assertTrue(repo.findStoreByID(1).getIsActive());

        repo.deleteStore(0);
        assertFalse(repo.findStoreByID(0).getIsActive());
        assertTrue(repo.findStoreByID(1).getIsActive());

        repo.deleteStore(1);
        assertFalse(repo.findStoreByID(0).getIsActive());
        assertFalse(repo.findStoreByID(1).getIsActive());
    }*/

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void addStoreAlreadyExists(IStoreRepository repo)  {
        repo.addStore("Alice", "American Eagle",  "Beer Sheva", "Eagle@gmail.com", "0548970173");

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            repo.addStore("Alice", "American Eagle",  "Beer Sheva", "Eagle@gmail.com", "0548970173");
        });

        String expectedMessage1 = "A store with the name American Eagle already exists.";
        assertEquals(expectedMessage1, expected1.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void storeExists(IStoreRepository repo)  {
        int id0 = repo.addStore("Alice", "American Eagle",  "Beer Sheva", "Eagle@gmail.com", "0548970173");
        int id1 = repo.addStore("Bob", "Shufersal",  "Beer Sheva", "Shufersal@gmail.com", "0548970173");
        int id2 = repo.addStore("Netta", "H&m",  "Beer Sheva", "Hm@gmail.com", "0548970173");
        int id3 = Math.max(id0, Math.max(id1, id2)) + 1;
        int id4 = id3 + 1;

        assertTrue(repo.storeExists(id0));
        assertTrue(repo.storeExists(id1));
        assertTrue(repo.storeExists(id2));
        assertFalse(repo.storeExists(id3));
        assertFalse(repo.storeExists(id4));
    }
}