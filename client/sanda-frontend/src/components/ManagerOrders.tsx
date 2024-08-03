import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const ManagerOrders = () => {
    const [username, setUsername] = useState('');
    const navigate = useNavigate();

    const handleSubmit = (event: React.FormEvent) => {
        event.preventDefault();
        if (username.trim()) {
            navigate(`/orders/${username}`);
        }
    };

    return (
        <div>
            <h1>Enter Username</h1>
            <form onSubmit={handleSubmit}>
                <input 
                    type="text" 
                    value={username} 
                    onChange={(e) => setUsername(e.target.value)} 
                    placeholder="Enter username" 
                    required 
                />
                <button type="submit">Submit</button>
            </form>
        </div>
    );
};

export default ManagerOrders;