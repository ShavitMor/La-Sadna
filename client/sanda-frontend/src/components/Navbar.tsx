import React, { useContext, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import '../styles/navbar.css';
import logo from '../images/la_sadna.png';
import SearchBar from './Search';
import '../styles/search.css';
import { Button, IconButton } from '@mui/material';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import { AppContext } from '../App';
import StoreSearchBar from './StoreSearchBar';
import { exitGuest } from '../API';

export const Navbar = () => {
    const {isloggedin , setIsloggedin } = useContext(AppContext);
    const navigate = useNavigate();
    const handleCartClick = () => {
        if(isloggedin){
          navigate(`/cart/${localStorage.getItem('username')}`);
        }else{
          navigate(`/cart/${localStorage.getItem('guestId')}`);
        }
      }
    const [showStoreSearch, setShowStoreSearch] = useState(false);

  const handleExitGuest = async () => {
        try {
            const response = await exitGuest(Number(localStorage.getItem('guestId'))); // Call exitGuest function
            if (response) {
                localStorage.removeItem('guestId'); // Clear guestId from localStorage
                alert('guest exited successfully.'); // Inform the user
            } else {
                console.error('Error exiting guest:', response.statusText); // Handle error response if needed
            }
        } catch (error: any) {
            console.error('Error exiting guest:', error.message); // Handle exception if request fails
        }
    };
          const handleToggleSearch = () => {
        setShowStoreSearch(prevShowStoreSearch => !prevShowStoreSearch);
    };
    return (
        <nav>
            <div className="navbar-container">
                <Link to="/" className="navbar-logo">
                    <img src={logo} width={160} height={100} alt="Logo"></img>
                </Link>
                  {showStoreSearch ? <StoreSearchBar /> : <SearchBar />}
                <Button onClick={handleToggleSearch} className="toggle-search-button">
                    {showStoreSearch ? 'Switch to Product Search' : 'Switch to Store Search'}
                </Button>
                <ul className="navbar-menu">
                    <li className="navbar-item">
                        <Link to="/login" className="navbar-link">
                            login
                        </Link>
                    </li>
                    <li className="navbar-item">
                        <Link to="/register" className="navbar-link">
                            register
                        </Link>
                    </li>
                     <li className="navbar-item">
                            <Button onClick={handleExitGuest} className="navbar-link">
                                Exit Guest
                            </Button>
                    </li>
                    <li className="navbar-item">
                        <IconButton size="small" color="inherit" onClick={handleCartClick}>
                        <ShoppingCartIcon />
                        </IconButton>
                    </li>
                </ul>
            </div>
        </nav>
    );
};

export default Navbar;