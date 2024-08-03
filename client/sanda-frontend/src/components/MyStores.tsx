// components/Stores.tsx
import React, { useEffect, useState, useContext } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { fetchUserStores, loginUsingJwt} from '../API';
import RoleModel from '../models/RoleModel';
import '../styles/stores.css';
import { AppContext } from '../App';

const Stores = () => {
    const { username } = useParams<{ username: string }>();
    const [stores, setStores] = useState<RoleModel[]>([]);
    const { isloggedin,setIsloggedin } = useContext(AppContext);
    const navigate=useNavigate();
    useEffect(() => {
        const fetchStores = async () => {
          if(!isloggedin){
            const resp=await loginUsingJwt(localStorage.getItem("username") as string, localStorage.getItem("token") as string);
                if(!resp.error){
                 setIsloggedin(true);
                }
                else{
                  localStorage.clear();
                  alert("Session over please login again");
                  navigate('/');
                }
          }
          const stores = await fetchUserStores(username as string);
          setStores(stores);
        };
        try{
            fetchStores();
        }catch{
          alert("Error occoured please try again later");
          navigate('/');
        }
      }, []);

    const handleCreateStore = () => {
        navigate('/createStore')
    };
    const handleEnterStore = (key:any) => {
        navigate(`/Store/${key}`)
    };

    return (
        <div className="stores-container">
            <h2>My Stores</h2>
            <button className="create-store-button" onClick={handleCreateStore}>Create New Store</button>
            <div className="stores-grid">
                {stores.map((store) => (
                    <div key={store.storeId} className="store-card" onClick={()=>handleEnterStore(store.storeId)}>
                        <h3>{store.storeName}</h3>
                        <p>Role: {store.role}</p>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default Stores;
