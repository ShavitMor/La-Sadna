package com.sadna.sadnamarket.domain.auth;

import java.util.HashMap;

public interface IAuthRepository {
    void login(String username,String password);
    HashMap<String,String> getAll();
    void add(String username,String password);
    void delete(String username);
    void clear();
}
