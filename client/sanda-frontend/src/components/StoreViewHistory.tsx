import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getOrders, getStoreOrderHistory } from '../API';
import '../styles/orders.css';
import { AdvancedOrderModel } from '../models/AdvancedOrderModel';

export const StoreViewHistory = () => {
    const { storeId } = useParams()
    const [orders, setOrders] = useState<AdvancedOrderModel[]>([]);

    useEffect(() => {
        async function fetchOrders() {

            const fetchedOrders = await getStoreOrderHistory(storeId!);
            setOrders(fetchedOrders);

        }
        fetchOrders();
    }, []);



    return (
        <div className="main">
            <div className="page-title">Store order history</div>
            <div className="orders-grid">
                {orders.map((order) => (
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
                                    <div className="product-image-container">

                                    </div>
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

export default StoreViewHistory;
