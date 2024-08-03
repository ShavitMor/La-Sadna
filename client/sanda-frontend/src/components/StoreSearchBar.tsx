import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getStoreByName } from '../API'; // Import the API function

export const StoreSearchBar = () => {
    const [storeName, setStoreName] = useState('');
    const navigate = useNavigate();

    const handleSearch = async () => {
        if (storeName.trim()) {
            try {
                const store = await getStoreByName(storeName, !localStorage.getItem('username') ? null : localStorage.getItem('username'));
                if (store) {
                    // Navigate to the search results page and pass the store data
                    navigate(`/search-stores-results`, { state: { store } });
                } else {
                    alert('Store not found!');
                }
            } catch (error) {
                console.error('Error fetching store:', error);
                alert('Error fetching store. Please try again later.');
            }
        }
    };

    return (
        <>
            <input 
                type="text" 
                placeholder="Search stores..." 
                value={storeName}
                onChange={(e) => setStoreName(e.target.value)}
                className="search-inputStore"
            />
            <button onClick={handleSearch} className="search-buttonStore">Search</button>
            </>
    );
};

export default StoreSearchBar;
