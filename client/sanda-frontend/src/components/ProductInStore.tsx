
import { useNavigate } from 'react-router-dom';

export const ProductInStore = (props: any) => {

    const navigate = useNavigate();
    return (
        <div className = "productbox" onClick={() => navigate(`/product/${props.product.productID}`)}>
            <h3 className = "product">{props.product.productName}</h3>
            <h4 className = "product">${props.product.productPrice}</h4>
        </div>
    );
};

export default ProductInStore;