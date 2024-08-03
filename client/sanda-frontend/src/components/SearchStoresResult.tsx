import React from 'react';
import { useLocation, Link } from 'react-router-dom';
import '../styles/searchStoresResult.css';

const SearchStoresResult = () => {
    const location = useLocation();
    const storeData = location.state?.store || null;
    const store = storeData ? JSON.parse(storeData.dataJson) : null;

    return (
        <div className="stores-results-container">
            <h2>Store Details</h2>
            {store ? (
                <div className="store-details">
                    <h3>{store.storeName}</h3>
                    <p>Rank: {store.rank}</p>
                    <p>Address: {store.address}</p>
                    <p>Email: {store.email}</p>
                    <p>Phone: {store.phoneNumber}</p>
                    <Link to={`/store/${store.storeId}`} className="enter-store-button">
                        Enter Store
                    </Link>
                </div>
            ) : (
                <p>No store found.</p>
            )}
        </div>
    );
};

export default SearchStoresResult;
