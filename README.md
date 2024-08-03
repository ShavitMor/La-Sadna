# La Sadna Market system
The system uses Configuration and Initial State files to set up the service. their syntax is specific in this file.

# Configuration Files
The system cannot start without the configuration file. It specifies important fields for the oepration of the system. The configuration files are saved in `code/src/main/resources`.
The configuration file for the main operational service (the REST API) is called `config.json`, and the configuration file for the Acceptance Tests is called `testconfig.json`.

The configuration files are a JSON format file, with fields relating to different aspects of the system's operations.

The different configuation fields are as follows:
- `db` - the URL of the database. This field is **MANDATORY** as the system cannot operate without a database. An attempt to launch the system without this field in the configuration will lead to an error.
- `sysman` - the username of the System Manager. This field is **MANDATORY** as the system cannot operate without a system manager. An attempt to launch the system without this field in the configuration will lead to an error. An attempt to appoint someone else as the system manager will also lead to an error.
- `supply` - the URL of the Supply API. This field is not mandatory, the supply interface will use a proxy by default.
- `payment` - the URL of the Payment API. This field is not mandatory, the payment interface will use a proxy by default.
- `clear` - if the value of this field is `on`, the system will clear the database on initialization. This is useful to be able to run an inital state without worrying about collisions with existing DB values. If the field is missing it will be off by default.
- `state` - the path to the Initial State file. If the field exists, the system will initialize with the initial state described in the file. Otherwise, the system will initiailze without an initial state.
- `concurrency_loop` - this field is only relevant to the Acceptance Tests. It dictates the amount of loops performed in the Concurrency Tests. It will be ignored for the operational service.
## Examples
```
{
  "sysman": "u1",
  "db": "jdbc:mysql://sadnadb.c1ueowgcy9zp.eu-west-3.rds.amazonaws.com:3306/sadna?useSSL=true",
  "supply": "https://damp-lynna-wsep-1984852e.koyeb.app/",
  "payment": "https://damp-lynna-wsep-1984852e.koyeb.app/",
  "clear": "on",
  "state": "state.json"
}
```
The above configuration file clears the system and starts it with the initial state described in the file `state.json`. It uses `https://damp-lynna-wsep-1984852e.koyeb.app/` for both supply and payment and uses the database `jdbc:mysql://sadnadb.c1ueowgcy9zp.eu-west-3.rds.amazonaws.com:3306/sadna?useSSL=true`. The system manager is a user called `u1`.

```
{
  "sysman": "u1",
  "db": "jdbc:mysql://sadnadb.c1ueowgcy9zp.eu-west-3.rds.amazonaws.com:3306/sadna?useSSL=true",
  "supply": "https://damp-lynna-wsep-1984852e.koyeb.app/",
  "payment": "https://damp-lynna-wsep-1984852e.koyeb.app/"
}
```
The above configuration uses the same settings for DB, supply, payment and the system manager, but does not start from an initial state, rather continues using the existing values of the DB.

```
{
  "sysman": "IAmAboutToBeGivenGreatPowers",
  "concurrency_loop": "100",
  "db": "jdbc:mysql://sadnadb.c1ueowgcy9zp.eu-west-3.rds.amazonaws.com:3306/sadna_test?useSSL=true"
}
```
The above configuration defines a user named `IAmAboutToBeGivenGreatPowers` as the system manager, and uses the Testing DB as the DB. Under this configuration, the supply and payment services will use a proxy, and the Concurrency Tests will perform 100 loops.

# Initial State Files
The Initial State files describe the initial state used for the initialization of the system. The files can have any name and be located anywhere, as long as they're referenced correctly by one of the configuration files.

An initial state is a collection of service methods performed at a given order at system initialization.

An Initial State file is a JSON format file, with one field: `commands`. The field holds a list of command objects, each with two fields:
- `method` - The name of the service method performed. The name is exactly the same as the name of the method in the MarketService class.
- `params` - An array of values, used as parameters for the method. The number of values and order must be exactly the same as the method's description in the MarketService class.

## Parameter Resolving
Some values, such as the Token of a logged in user or the ID of a store, cannot be known while writing the state file, as their value is decided in runtime.

To address this, we mark these values a stand-in syntax, and their value is resolved in runtime.

- To get the token of a user, use `token-username`, where `username` is the username of the user.
- To get the ID of a store, use `storeId-index`, where `index` is the index of the store, by creation order (1 is the first store created).
- To get the ID of a request, such as a manager or owner appointment request, use `request-index`, where `index` is the index of the request. (1 is the first request of the last user who we got the notifications of).
-  To get the ID of a guest, use `guestId-index`, where `index` is the index of the guest. (1 is the first guest).
-  To get the Enum value of a permission, use `permission-index`, where `index` is the index of the permission. (1 is the first permission).
## Examples
```
{
"commands": [
      {
        "method": "register",
        "params": ["u1", "u1Password", "user","one","u1@gmail.com","0501111111","12-09-1998"]
      },
      {
        "method": "setSystemAdminstor",
        "params": ["u1"]
      },
      {
        "method": "login",
        "params": ["u1", "u1Password"]
      },
      {
        "method": "createStore",
        "params": ["token-u1","u1","s1", "bgu 1 beer sheva","u1@gmail.com","0502222222"]
      },
      {
        "method": "addProductToStore",
        "params": ["token-u1", "u1", "storeId-1", "Bamba", 20, 3.5, "Food", 3.0,  0.5, "Bamba the loveable snake"]
      }
]
}
```
In this example, we register and login the system administrator, `u1`, and then use his token, `token-u1`, to create a store, `s1`, and then use the store's id, `storeId-1`, to add a product to the store.

```
...............
      {
        "method": "sendStoreManagerRequest",
        "params": ["token-u2", "u2","u3","storeId-1"]
      },
      {
        "method": "login",
        "params": ["u3", "u3Password"]
      },
      {
        "method": "getUserNotifications",
        "params": ["u3"] 
      },
      {
        "method": "acceptRequest",
        "params": ["u3", "request-1"] 
      },.........
```
In this example, we send a store manager request to u3. We then login u3 so we can accept the request, load his notifications by using `getUserNotifications`, and then accepting the first of these requests, using the request ID, `request-1`
