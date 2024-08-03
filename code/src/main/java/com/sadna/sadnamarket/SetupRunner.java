package com.sadna.sadnamarket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna.sadnamarket.api.Response;
import com.sadna.sadnamarket.domain.users.NotificationDTO;
import com.sadna.sadnamarket.domain.users.Permission;
import com.sadna.sadnamarket.domain.users.RequestDTO;
import com.sadna.sadnamarket.service.MarketService;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.format.DateTimeFormatter;

public class SetupRunner {
    private MarketService service;
    private Map<String, String> users;
    private List<Integer> stores;
    private List<Integer> products;
    private List<Integer> guests;
    private List<RequestDTO> requests;
    private ObjectMapper objectMapper;
    private DateTimeFormatter dateFormatter;




    public SetupRunner(MarketService service) {
        this.service = service;
        this.users = new HashMap<>();
        this.stores = new ArrayList<>();
        this.products = new ArrayList<>();
        this.requests = new ArrayList<>();
        this.guests = new ArrayList<>();
        this.objectMapper = new ObjectMapper();
        this.dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }
    public void setupFromJson(String filename) {  
       try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + filename);
            }
            Map<String, List<Map<String, Object>>> data = objectMapper.readValue(inputStream, new TypeReference<>() {});
            List<Map<String, Object>> commands = data.get("commands");

            for (Map<String, Object> command : commands) {
                String methodName = (String) command.get("method");
                List<Object> params = (List<Object>) command.get("params");
                List<Object> resolvedParams = resolveParams(params);

                Response res = executeMethod(methodName, resolvedParams);

                if(res.getError()){
                    throw new UnsupportedOperationException("Start from state failed at command " + command.get("method") + ": " + res.getErrorString());
                }

                if (methodName.equals("login")) {
                    users.put((String) params.get(0), res.getDataJson());
                } else if (methodName.equals("createStore")) {
                    stores.add(Integer.parseInt(res.getDataJson()));
                } else if (methodName.equals("addProductToStore")) {
                    products.add(Integer.parseInt(res.getDataJson()));
                } else if (methodName.equals("getUserNotifications")) {
                    requests = objectMapper.readValue(res.getDataJson(), new TypeReference<List<RequestDTO>>() {});
                }
                else if (methodName.equals("enterAsGuest")||methodName.equals("logout")) {
                    guests.add(Integer.parseInt(res.getDataJson()));                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private List<Object> resolveParams(List<Object> params) {
        List<Object> resolvedParams = new ArrayList<>();
        for (Object param : params) {
            if (param instanceof String) {
                String paramStr = (String) param;
                if (paramStr.startsWith("token-")) {
                    String username = paramStr.substring(6);
                    resolvedParams.add(users.get(username));
                } else if (paramStr.startsWith("storeId-")) {
                    int storeIndex = Integer.parseInt(paramStr.substring(8)) - 1;
                    resolvedParams.add(stores.get(storeIndex));
                }
                else if (paramStr.startsWith("guestId-")) {
                        int guestIndex = Integer.parseInt(paramStr.substring(8)) - 1;
                        resolvedParams.add(guests.get(guestIndex));
                        guests.remove(guestIndex);
                    
                } else if (paramStr.startsWith("request-")) {
                    int requestIndex = Integer.parseInt(paramStr.substring(8)) - 1;
                    resolvedParams.add(requests.get(requestIndex).getId());
                
                }else if (isValidDate(paramStr)) {
                    resolvedParams.add(LocalDate.parse(paramStr, dateFormatter));
                }
                else if (paramStr.startsWith("permission-")) {
                String[] permissionsArray = paramStr.substring(11).split(",");
                Integer[] permissionsArrayInt= new Integer[permissionsArray.length];
                for (int i = 0; i < permissionsArray.length; i++) {
                    permissionsArrayInt[i]=Integer.parseInt(permissionsArray[i]);
                }
                Set<Permission> permissions = new HashSet<Permission>();
                for (Integer permissionStr : permissionsArrayInt) {
                    permissions.add(Permission.getEnumByInt(permissionStr));
                }
                resolvedParams.add(permissions);
            }
                 else {
                    resolvedParams.add(paramStr);
                }
            } else {
                resolvedParams.add(param);
            }
        }
        return resolvedParams;
    }
    private Response executeMethod(String methodName, List<Object> params) {
        try {
            Class<?>[] paramTypes = params.stream()
                    .map(param -> {
                        if (param instanceof Integer) {
                            return int.class;
                        } else if (param instanceof Boolean) {
                            return boolean.class;
                        } else if (param instanceof Long) {
                            return long.class;
                        } else if (param instanceof Double) {
                            return double.class;
                        } else if (param instanceof Float) {
                            return float.class;
                        } else if (param instanceof LocalDate) {
                            return LocalDate.class;
                        } else if (param instanceof String) {
                            return String.class;
                        } else {
                            return param.getClass();
                        }
                    })
                    .toArray(Class<?>[]::new);

            Method method = service.getClass().getMethod(methodName, paramTypes);
            System.out.println("run method: " + methodName + " with parameters " + params);
            return (Response) method.invoke(service, params.toArray());
        } catch (NoSuchMethodException e) {
            System.err.println("Method not found: " + methodName + " with parameters " + params);
            e.printStackTrace();
            throw new RuntimeException("Failed to execute method: " + methodName, e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to execute method: " + methodName, e);
        }
    }
    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr, dateFormatter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
