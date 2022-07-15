## Usage

#### 1. Requisites:

```
Java version -> openjdk version 18.0.1.1
Operating system -> Windows
```
#### 2. Run the script file in order to compile the program:
```
$ ./script.sh
```

#### 3. Execute the desired command (command prompt):

```
Store:
$ java -cp ./out;lib/json-20220320.jar store.Store <IP_mcast_addr> <IP_mcast_port> <node_id> <Store_port>

Test Client:
$ java -cp ./out;lib/json-20220320.jar client.TestClient <node_ap> <operation> [<opnd>]

<node_ap> -> <node_id>:<Store_port>
<operation> -> Must be upper case
```