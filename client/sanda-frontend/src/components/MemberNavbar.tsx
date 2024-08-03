import React, { useContext, useEffect, useRef, useState } from 'react';
import '../styles/memberNavbar.css';
import { Link, useNavigate } from 'react-router-dom';
import { IconButton, Badge, Button } from '@mui/material';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { acceptRequest, checkIsSystemManager, enterAsGuest, fetchNotifications, logout, okNotification, rejectRequest } from '../API';
import { AppContext } from '../App';
import { NotificationModel, RequestModel } from '../models/NotificationModel';
import { useSubscription } from 'react-stomp-hooks';
import SearchBar from './Search';
import logo from '../images/la_sadna.png';
import StoreSearchBar from './StoreSearchBar';


const MemberNavbar = () => {
  const {isloggedin , setIsloggedin } = useContext(AppContext);
  const [menuOpen, setMenuOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [notifications, setNotifications] = useState<NotificationModel[]>([]);
    const [isSystemManager, setIsSystemManager] = useState(false); // State to track if the user is a system manager

  const navigate = useNavigate();

  useSubscription(`/topic/notifications/${localStorage.getItem('username')}`, (message) => {
    let notif: NotificationModel = JSON.parse(message.body);
    alert(`New Notification: ${notif.message}`);
    console.log(notif);
    reloadNotifs();
  })

  useEffect(() => {
    reloadNotifs()
  }, [])

  useEffect(() => {
    const checkRole = async () => {
      const username = localStorage.getItem('username');
      if (username) {
        const isManager = await checkIsSystemManager(username);
        setIsSystemManager(isManager);
      }
    };

    checkRole();
  }, []); // This effect will run once when the component mounts


  const reloadNotifs = async () => {
    setNotifications(await fetchNotifications())
    {console.log(notifications)}
  }

  const toggleMenu = () => {
    setMenuOpen(!menuOpen);
  };

  const toggleNotifications = () => {
    setNotificationsOpen(!notificationsOpen);
  };
  const handleLogout = async () => {
    try{
    const response=await logout(localStorage.getItem('username') as string);
    console.log(response);
    localStorage.clear();
    localStorage.setItem("guestId", `${response}`);
    }catch(e){
      alert("Error occoured");
    }
    setIsloggedin(false);
    navigate('/');
  }
  const getProfileUrl = ():string => {
    return `/profile/${localStorage.getItem('username')}`;
  }
  const getStoresUrl = ():string => {
    return `/memberStores/${localStorage.getItem('username')}`;
  }
  const getOrdersUrl = ():string => {
    return `/orders/${localStorage.getItem('username')}`};
  const handleCartClick = () => {
    if(isloggedin){
      navigate(`/cart/${localStorage.getItem('username')}`);
    }else{
      navigate(`/cart/${localStorage.getItem('guestId')}`);
    }
  }
   const handleToggleSearch = () => {
        setShowStoreSearch(prevShowStoreSearch => !prevShowStoreSearch);
    };
   const handleGetAllOrdersClick = () => {
        return '/managerUserHistory';
    };
    const handleGetStoreOrdersClick = () => {
      return '/managerStoreHistory';
  };
      const [showStoreSearch, setShowStoreSearch] = useState(false);

  return (
    <nav className="membernavbar" onClick={()=>{menuOpen&&toggleMenu();notificationsOpen&&toggleNotifications();}}>
      <Link to="/" className="navbar-logo">
        <img src={logo} width={160} height={100} alt="Logo"></img>
      </Link>
         {showStoreSearch ? <StoreSearchBar /> : <SearchBar />}
              <Button onClick={handleToggleSearch} className="toggle-search-button">
                    {showStoreSearch ? 'Switch to Product Search' : 'Switch to Store Search'}
                </Button>
      <div className="navbar-right">
        <div className="notifications-navbar-items">
          <IconButton size="small" color="inherit" onClick={toggleNotifications}>
            <Badge badgeContent={notifications.length} color="error">
              <NotificationsIcon />
            </Badge>
          </IconButton>
          {notificationsOpen && (
            <ul className="menu">
              {notifications.map((notification, index) => (
                <li key={index} className="notification-item">
                  <p>{notification.message}</p>
                  {!('storeId' in notification) && <button onClick={() => {
                    okNotification(notification.id)
                    setNotifications(notifications.filter(notif => notif.id != notification.id))
                  }} className="">OK </button>}
                  {('storeId' in notification) && <button onClick={() => {
                    acceptRequest(notification.id)
                    setNotifications(notifications.filter(notif => notif.id != notification.id))
                    }} className="">accept </button>}
                  {('storeId' in notification) && <button onClick={() => {
                    rejectRequest(notification.id)
                    setNotifications(notifications.filter(notif => notif.id != notification.id))
                  }} className="">reject </button>}
                </li>
              ))}
            </ul>
          )}
        </div>
        <div className="member-navbar-items">
          <button className="menu-toggle" onClick={toggleMenu}>
            ☰
          </button>
          {menuOpen && (
            <ul className="options-menu">
              <li className="menu-item"><Link to={getOrdersUrl()} className="navbar-link">My Orders</Link></li>
              <li className="menu-item"><Link to={getStoresUrl()} className="navbar-link">My Stores</Link></li>
              <li className="menu-item"><Link to={getProfileUrl()} className="navbar-link">Profile</Link></li>
               {isSystemManager && (
                <>
                  <li className="menu-item"><Link to={handleGetAllOrdersClick()} className="navbar-link">Get User Orders</Link></li>
                  <li className="menu-item"><Link to={handleGetStoreOrdersClick()} className="navbar-link">Get Store Orders</Link></li>
                </>
              )} 
             
              <li className="menu-item"onClick={handleLogout}>logout</li>
            </ul>
          )}
        </div>
        <IconButton size="small" color="inherit" onClick={handleCartClick}>
          <ShoppingCartIcon />
        </IconButton>
      </div>
    </nav>
  );
};

export default MemberNavbar;
