import React, { useContext, useState } from 'react';
import { login, loginFromGuest } from '../API';
import { useNavigate } from 'react-router-dom'; // Import the useNavigate hook
import '../styles/login.css';
import { AppContext } from '../App';


export const Login = () => {
    const { setIsloggedin } = useContext(AppContext);
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleUsernameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setUsername(e.target.value);
    };

    const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setPassword(e.target.value);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            let resp;
            if(localStorage.getItem("guestId") === null){
            resp = await login(username, password); // Assuming login is an async function
            }else{
            resp = await loginFromGuest(username, password,Number.parseInt(localStorage.getItem("guestId") as string)); // Assuming login is an async function
            }
            if (resp.error) {
                alert("Error: " + resp.errorString);
            } else {
                setIsloggedin(true);
                localStorage.setItem('token', resp.dataJson); // Save the token in local storage
                localStorage.setItem('username', username); // Save the username in local storage
                localStorage.removeItem('guestId')
                navigate('/'); // Navigate to the home page
            }
        } catch (error) {
            alert("Error ocoured ");
        }
    };

    return (
        <div className="login-container">
            <h2>Login</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="username">Username:</label>
                    <input type="text" id="username" value={username} onChange={handleUsernameChange} />
                </div>
                <div>
                    <label htmlFor="password">Password:</label>
                    <input type="password" id="password" value={password} onChange={handlePasswordChange} />
                </div>
                <button type="submit" onClick={handleSubmit}>Login</button>
            </form>
        </div>
    );
};

export default Login;
