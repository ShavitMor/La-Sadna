import './App.css';
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Login from './components/Login';
import Register from './components/Register';
import Home from './components/Home';
import Navbar from './components/Navbar';
import MemberNavbar from './components/MemberNavbar';
import { useState, createContext, useEffect, useRef } from 'react';
import Profile from './components/Profile';
import SearchResults from './components/SearchResults';
import Store from './components/Store';
import Staff from './components/Staff';
import Cart from './components/Cart';
import Orders from './components/Orders';
import MyStores from './components/MyStores';
import { Create } from '@mui/icons-material';
import CreateStore from './components/CreateStore';
import { StompSessionProvider } from 'react-stomp-hooks';
import DiscountWizard from './components/DiscountWizard';
import PermissionError from './components/PermissionError';
import { enterAsGuest, isGuestExists, loginUsingJwt } from './API';
import BuyPolicyWizard from './components/BuyPolicyWizard';
import AddProduct from './components/AddProduct';
import { Product } from './components/Product';
import SetBankAccount from './components/SetBankAccount';
import StoreOrders from './components/StoreOrders';
import Purchase from './components/Purchase';
import SearchOrdersByUsername from './components/OrderHistoryByUser';
import StoreViewHistory from './components/StoreViewHistory';
import SearchStoresResult from './components/SearchStoresResult';
import EditProduct from './components/EditProduct';
import CowComponent from './components/CowComponent';
import { useSelector } from 'react-redux';

interface AppContextProps {
  isloggedin: boolean;
  setIsloggedin: React.Dispatch<React.SetStateAction<boolean>>;
}

export const AppContext = createContext<AppContextProps>({
  isloggedin: false,
  setIsloggedin: () => {},
});


function App() {
  const [isloggedin, setIsloggedin] = useState(false);
  const effectRan = useRef(false);
  const conerror = useSelector((state:any) => state.conerror.value)
  
  useEffect(() => {
    const fetchData = async () => {
      if (effectRan.current) return;          
      if(isloggedin) return;
      if(localStorage.getItem("guestId")){
        try{
        const res=await isGuestExists(Number.parseInt(localStorage.getItem("guestId") as string));
        if(!JSON.parse(res.dataJson)){
          alert("Error occoured refreshing");
          localStorage.clear();
        }
        else{
          return;
        }
      }catch(e){
        alert("Error occoured please try again later");
        localStorage.clear();
      }
      }
      if(localStorage.getItem("token") !== null&& localStorage.getItem("username") !== "null"){
        try{
        const resp=await loginUsingJwt(localStorage.getItem("username") as string, localStorage.getItem("token") as string);
        if(!resp.error){
         setIsloggedin(true);
         return;
        }
        else{
          localStorage.clear();
          alert("Session over please login again");
        }
      }catch(e){
        alert("Error occoured please try again later");      
      }}
      try{
      const guestId = await enterAsGuest();
      localStorage.setItem("guestId", `${guestId}`);
      }catch(e){
        alert("Error occoured please try again later");
      }
    };
    
    fetchData();
    effectRan.current = true;
  }, [isloggedin, setIsloggedin]);
  return (
    <div className="App">
      {conerror ? <CowComponent/> : 
      <AppContext.Provider value={{ isloggedin, setIsloggedin }}>
      <Router>
        {isloggedin ? 
        <StompSessionProvider url={'http://127.0.0.1:8080/ws'}><MemberNavbar /></StompSessionProvider>
         : <Navbar />}
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/profile/:username" element={<Profile />} />
          <Route path="/orders/:username" element={<Orders />} />
          <Route path="/cart/:username" element={<Cart />} />
          <Route path="/search-results" element={<SearchResults />} />
          <Route path="/store/:storeId" element={<Store/>}/>
          <Route path="/product/:productId" element={<Product/>}/>
          <Route path="/editproduct/:productId" element={<EditProduct/>}/>
          <Route path="/store/:storeId/staff" element={<Staff/>}/>
          <Route path="/store/:storeId/policy" element={<BuyPolicyWizard/>}/>
          <Route path="/store/:storeId/discount" element={<DiscountWizard/>}/>
          <Route path="/store/:storeId/addProduct" element={<AddProduct />}/>
          <Route path="/store/:storeId/bank" element={<SetBankAccount />}/>
          <Route path="/store/:storeId/orders" element={<StoreViewHistory />}/>
          <Route path="/memberStores/:username" element={<MyStores />} />
          <Route path="/createStore" element={<CreateStore />}/>
          <Route path="/purchase" element={<Purchase />}/>
          <Route path="/permission-error" element={<PermissionError />}/>
          <Route path="/managerStoreHistory" element={<StoreOrders />}/>
          <Route path="/managerUserHistory" element={<SearchOrdersByUsername />}/>
          <Route path="/search-stores-results" element={<SearchStoresResult />}/>
          <Route path="/*" element={<h1>PAGE NOT FOUND!</h1>} />
        </Routes>
      </Router>
    </AppContext.Provider>}
    </div>
  );
}

export default App;
