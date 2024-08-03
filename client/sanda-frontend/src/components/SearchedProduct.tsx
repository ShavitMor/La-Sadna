import React, { useState,useContext  } from 'react';
import '../styles/searched-product.css';
import '../styles/general.css';
import ProductModel from '../models/ProductModel';
import { addProductToCartGuest, addProductToCartMember } from '../API';
import { AppContext } from '../App';
import RestResponse from '../models/RestResponse';
import { useNavigate } from 'react-router-dom';

interface SearchedProductProps {
  product: ProductModel;
}

const SearchedProduct: React.FC<SearchedProductProps> = ({ product }) => {
  const { isloggedin } = useContext(AppContext);
  const [addedToCart, setAddedToCart] = useState(false);
  const [selectedQuantity, setSelectedQuantity] = useState<number>(1);
  const navigate = useNavigate();

  async function handleAddToCart(event: React.MouseEvent<HTMLButtonElement, MouseEvent>) {
    let response;
      if(isloggedin){
      response = await addProductToCartMember(product.productID, product.storeId ? product.storeId : 0, selectedQuantity);

      }
      else{
        response = await addProductToCartGuest(product.productID, product.storeId ? product.storeId : 0, selectedQuantity);
      }
      if((response as RestResponse).error){
        alert((response as RestResponse).errorString);
      }else{
      console.log('Product added to cart:', product);
      setAddedToCart(true);
      }
  }

  function handleQuantityChange(event: React.ChangeEvent<HTMLSelectElement>): void {
    setSelectedQuantity(Number(event.target.value));
  }

  return (
    <div className="product-container">
      <div className="product-name limit-text-to-2-lines">
        {product.productName}
      </div>

      <div className="product-description">
        {product.description}
      </div>

      <div className="product-rating-container">
        <img
          className="product-rating-stars"
          src={require(`../images/ratings/rating-${Math.round((product.productRank ?? 0) * 2) * 5}.png`)}
          alt="Rating"
        />
        <div className="product-rating-count link-primary">
          {product.productRank}
        </div>
      </div>

      <div className="product-price">
        ${product.productPrice.toFixed(2)}
      </div>

      <div className="product-quantity-container">
        <select value={selectedQuantity} onChange={handleQuantityChange}>
          {[...Array(15)].map((_, index) => (
            <option key={index} value={index + 1}>
              {index + 1}
            </option>
          ))}
        </select>
      </div>

      <div className={addedToCart ? "added-to-cart-visible" : "added-to-cart"}>
        <img src={require("../images/ratings/checkmark.png")} alt="Checkmark" />
        Added
      </div>

      <button className="add-to-cart-button button-primary" onClick={handleAddToCart}>
        Add to Cart
      </button>
      <button className="add-to-cart-button button-secondary" onClick={() => navigate(`/product/${product.productID}`)}>
        Go to page
      </button>
    </div>
  );
};

export default SearchedProduct;

