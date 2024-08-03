import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { checkIsSystemManager, getOrders, getStoreOrderHistory, getStoreOrders } from '../API'; 
import '../styles/storeOrders.css'; 
import { AdvancedOrderModel } from '../models/AdvancedOrderModel';

export const StoreOrders = () => {
    const [storeOrders, setStoreOrders] = useState<AdvancedOrderModel[]>([]);
    const [storeId, setStoreId] = useState<string>('');
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

    const handleStoreIDChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setStoreId(event.target.value);
    };

    const handleStoreSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        if (storeId.trim()) {
            const fetchedOrders = await getStoreOrderHistory(storeId);
            setStoreOrders(fetchedOrders);
        } else {
            setStoreOrders([]);
        }
    };


    return (
        <div className="main">
                        <div className="page-title">Search Orders by Store ID</div>
            <form className="search-form" onSubmit={handleStoreSubmit}>
                <input
                    type="number"
                    placeholder="Enter store ID..."
                    value={storeId}
                    onChange={handleStoreIDChange}
                    className="search-input"
                />
                <button type="submit" className="submit-button">Search</button>
            </form>
            <div className="orders-grid">
                {storeOrders.map((order) => (
                    <div className="order-container" key={order.id}>
                        <div className="order-header">
                            <div className="order-header-left-section">
                                <div className="order-total">
                                    <div className="order-header-label">Ordered By:</div>
                                    <div>{order.memberName}</div>
                                </div>
                            </div>
                            <div className="order-header-right-section">
                                <div className="order-header-label">Order ID:</div>
                                <div>{order.id}</div>
                            </div>
                        </div>
                        <div className="order-details-grid">
                            {order.products!.map((product) => (
                                <React.Fragment key={product.id}>
                                    <div className="product-details">
                                        <div className="product-name">{product.name}</div>
                                        <div className="product-quantity">Quantity: {product.amount}</div>
                                    </div>

                                    <div className="product-actions">
                                        <div className="product-price">Price Before Discount: ${product.oldPrice}</div>
                                        <div className="product-price">Price After Discount: ${product.newPrice}</div>
                                        <div className="product-price">Total Product Price: ${product.newPrice * product.amount}</div>
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

export default StoreOrders;
