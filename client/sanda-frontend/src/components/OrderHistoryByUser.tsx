import React, { useEffect, useState } from 'react';
import { checkIsSystemManager, getOrders, getStoreOrderHistory } from '../API';
import { OrderModel } from '../models/OrderModel';
import '../styles/userHistoryOrders.css';
import { useNavigate } from 'react-router-dom';
import { AdvancedOrderModel } from '../models/AdvancedOrderModel';

const SearchOrdersByUsername = () => {
    const [orders, setOrders] = useState<OrderModel[]>([]);
    const [username, setUsername] = useState<string>('');
    const navigate = useNavigate();

    useEffect(() => {
        const checkAllowed = async () => {
            let canAccess: boolean = await checkIsSystemManager((localStorage.getItem("username") ? localStorage.getItem("username") : "")!);
            if (!canAccess) {
                navigate('/permission-error', { state: "You are not the system manager" })
            }
        }
        checkAllowed();
    }, [])

    const handleUsernameChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setUsername(event.target.value);
    };

    const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        if (username.trim()) {
            const fetchedOrders = await getOrders(username);
            setOrders(fetchedOrders);
        } else {
            setOrders([]);
        }
    };

    return (
        <div className="main">
            <div className="page-title">Search Orders by Username</div>
            <form className="search-form" onSubmit={handleSubmit}>
                <input
                    type="text"
                    placeholder="Enter username..."
                    value={username}
                    onChange={handleUsernameChange}
                    className="search-input"
                />
                <button type="submit" className="submit-button">Search</button>
            </form>
            <div className="orders-grid">
                {orders.map((order) => (
                    <div className="order-container" key={order.id}>
                        <div className="order-header">
                            <div className="order-header-left-section">
                                <div className="order-date">
                                    <div className="order-header-label">Order Placed:</div>
                                    <div>{order.date}</div>
                                </div>
                                <div className="order-total">
                                    <div className="order-header-label">Total:</div>
                                    <div>${order.total}</div>
                                </div>
                            </div>
                            <div className="order-header-right-section">
                                <div className="order-header-label">Order ID:</div>
                                <div>{order.id}</div>
                            </div>
                        </div>
                        <div className="order-details-grid">
                            {order.products.map((product) => (
                                <React.Fragment key={product.id}>
                                    <div className="product-image-container">

                                    </div>
                                    <div className="product-details">
                                        <div className="product-name">{product.name}</div>
                                        <div className="store-name">Store ID: {product.storeId}</div>
                                        <div className="product-quantity">Quantity: {product.quantity}</div>
                                    </div>

                                    <div className="product-actions">
                                        <div className="product-price">Price Before: ${product.oldPrice}</div>
                                        <div className="product-price">Price After: ${product.newPrice}</div>
                                    </div>
                                </React.Fragment>
                            ))}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default SearchOrdersByUsername;
