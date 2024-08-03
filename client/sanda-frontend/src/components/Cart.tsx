import React, { useContext, useEffect, useState } from 'react';
import '../styles/cart.css';
// Assuming these interfaces and functions are imported correctly
import { viewMemberCart, viewGuestCart, changeProductAmountInCart, changeProductAmountInCartGuest, removeProductFromCart, removeProductFromCartGuest, checkCart, checkCartGuest, loginUsingJwt, isGuestExists, getProductAmount } from '../API'; // Add necessary API functions
import cartModel from '../models/CartModel';
import { useNavigate, useParams } from 'react-router-dom';
import { AppContext } from '../App';
import CustomizedDialogs from './CartError';
import { bool } from 'yup';

const Cart = () => {
    const {isloggedin , setIsloggedin } = useContext(AppContext);
    const { username } = useParams<{ username: string }>();
    const [cart, setCart] = useState({ productsData: [], oldPrice: 0, newPrice: 0 } as cartModel);
    const navigate = useNavigate();
    const [dialogOpen, setDialogOpen] = useState(false);
    const [error, setError] = useState("");
    const [showedError, setShowed] = useState(false);
    const [productsMaxAmount, setProductsMaxAmount] = useState<{[id: string]: number}>({});

    

    useEffect(() => {
        const checkLogin = async () => {
            if(!isloggedin&&!localStorage.getItem("guestId")){
              const resp=await loginUsingJwt(localStorage.getItem("username") as string, localStorage.getItem("token") as string);
              console.log(resp);
                  if(!resp.error){
                   setIsloggedin(true);
                   await fetchCart(true);
                await checkCart(true);
                  }
                  else{
                    localStorage.clear();
                    alert("Session over please login again");
                    navigate('/');
                  }
            }
            else if(!isloggedin&&localStorage.getItem("guestId")){
            try{
                const data=await isGuestExists(Number.parseInt(localStorage.getItem("guestId") as string));
                if(JSON.parse(data.dataJson)===true){
                await fetchCart(isloggedin);
                await checkCart(isloggedin);
                }
                else{
                    localStorage.clear();
                    alert("cart not found please try again");
                    navigate('/');
                    navigate(0);
                }
            }catch(e: any){
                console.log(e);
                alert("Error occoured please try again later");
                navigate('/');
            }
        }
            else{
                await fetchCart(isloggedin);
                await checkCart(isloggedin);
            }        
          };   
    const checkCart=async(isloggedin: boolean)=>{
        await validate(isloggedin);
    }
    checkLogin()
    }, []);

    useEffect(() => {
        if(error != ""){setDialogOpen(true);}
    }, [error])

    const fetchCart = async (isloggedin: boolean ) =>{ 
        try{
        let response;
        console.log(isloggedin);
        if(isloggedin){
            response = await viewMemberCart(localStorage.getItem("username")!);
        }
        else{
            response = await viewGuestCart(parseInt(localStorage.getItem("guestId")!)); 
        }
        console.log(response);
        const res=JSON.parse(response.dataJson);
        if(res.error){
            alert(res.errorString);
        }
        setCart(res as cartModel);
        let productsAmountsDict: {[id: string]: number} = {};
        for(const product of (res as cartModel).productsData){
            let max = await getProductAmount(product.id, product.storeId);
            productsAmountsDict[`${product.id}`] = max;
        }
        setProductsMaxAmount(productsAmountsDict);
    }catch(e: any){
        alert("Error occoured please try again later");
        navigate('/');
    }
    }
    const handleQuantityChange = async(index: number, event: React.ChangeEvent<HTMLSelectElement>) => {
        try{
        const updatedCart = { ...cart };
        updatedCart.productsData[index].amount = parseInt(event.target.value, 10);
        let res;
        if(isloggedin)
            res=await changeProductAmountInCart(updatedCart.productsData[index].id,updatedCart.productsData[index].storeId, updatedCart.productsData[index].amount)
        else{
            res=await changeProductAmountInCartGuest(updatedCart.productsData[index].id,updatedCart.productsData[index].storeId, updatedCart.productsData[index].amount)
        }
        validate(isloggedin)
        if (res.error){
            alert(res.errorString);
        }
        else{
            await fetchCart(isloggedin)
        }
    }catch(e: any){
        console.log(e);
        alert("Error occoured please try again later");
    }
    };

    const handleRemoveProduct = async(index: number) => {
        try{
        const updatedCart = { ...cart };
        let res;
        if(isloggedin)
            res=await removeProductFromCart(updatedCart.productsData[index].id,updatedCart.productsData[index].storeId);
        else
            res=await removeProductFromCartGuest(updatedCart.productsData[index].id,updatedCart.productsData[index].storeId);
            if (res.error){
                alert(res.errorString);
            }
            else{
                await fetchCart(isloggedin)
            }
        }
    catch(e: any){
        console.log(e);
        alert("Error occoured please try again later");
    }
    };
    const validate=async(isloggedin: boolean )=>{
        try{
            let response;
            if(isloggedin){
                response = await checkCart(username as string);
            }
            else{
                response = await checkCartGuest(Number.parseInt(username as string));  
        }
        if(response.error){
            setError(response.errorString);
        }else{
            setError("")
        }
}
        catch(e: any){
            console.log(e);
            alert("Error occoured please try again later");
        
        }
    }
    const handleDialogClose = () => {
        setDialogOpen(false);
      };
    if (!cart) return <div>Loading...</div>;

    return (
        <div className="cart-container">
            <h2>Shopping Cart</h2>
            <div className="cart-items">
                {cart.productsData.map((product, index) => (
                    <div key={index} className="cart-item">
                        <div className="item-details">
                            <h3>{product.name}</h3>
                            <div className="item-quantity">
                                <label htmlFor={`quantity-${index}`}>Quantity: </label>
                                <select
                                    id={`quantity-${index}`}
                                    value={product.amount}
                                    onChange={(event) => handleQuantityChange(index, event)}
                                >
                                    {[...Array(productsMaxAmount[product.id])].map((_, n) => (
                                        <option key={n + 1} value={n + 1}>
                                            {n + 1}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <p>Price: ${product.oldPrice}</p>
                            <p>Discounted Price: ${product.newPrice}</p>
                        </div>
                        <button className="button remove" onClick={() => handleRemoveProduct(index)}>X</button>
                    </div>
                ))}
            </div>
            <div className="cart-summary">
                <h3>Cart Summary</h3>
                <p>Total Price: ${cart.oldPrice}</p>
                <p>Discounted Price: ${cart.newPrice}</p>
                {error != "" && <p style={{color: 'red'}}>You cannot purchase the cart: {error}</p>}
                <button onClick = {() => navigate('/purchase')} className="purchase-button" disabled={error != "" &&cart.productsData.length>0} >Purchase</button>
            </div>
            <CustomizedDialogs open={dialogOpen} onClose={handleDialogClose} text={error} /> 
        </div>
    );
};

export default Cart;
