import React, { useContext, useEffect, useState } from 'react';
import { useForm, SubmitHandler } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import '../styles/CreateStore.css';
import { addProduct, createNewStore, hasPermission, loginUsingJwt } from '../API';
import { useNavigate, useParams } from 'react-router-dom';
import { AppContext } from '../App';
import Permission from '../models/Permission';
import { Categories } from '../models/CategoriesConst';
import { Loading } from './Loading';

// Define the schema for validation
const schema = yup.object().shape({
  productName: yup.string().required('Product name is required'),
  productPrice: yup.number().positive('Product Price must be above positive').required('Product price is required'),
  category: yup.string().required('Product category is required'),
  productWeight: yup.number().positive('Product Weight must be above positive').required('Product weight is required'),
  productQuantity: yup.number().integer('Initial Amount must be whole').positive('Initial Amount must be positive').required('Product category is required'),
  description: yup.string()
});

const AddProduct = () => {
  const { register, handleSubmit, formState: { errors, touchedFields }, trigger } = useForm({
    resolver: yupResolver(schema)
  });

  const {storeId} = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);


  useEffect(() => {
    const checkAllowed = async ()=> {
        let canAccess: boolean = await hasPermission(storeId!, Permission.ADD_PRODUCTS);
        if(!canAccess){
            navigate('/permission-error', {state: "You do not have permission to add products in the given store"})
        }
    }
    checkAllowed();
}, [])

  const onSubmit: SubmitHandler<any> = async (data) => {
    const response = await addProduct(data, parseInt(storeId!));
    setLoading(true);
    if (response.error) {
      alert("Error: " + response.errorString);
    } else {
      alert("Product created successfully!");
      navigate(`/store/${storeId}`);
    }
    setLoading(false);
  };

  return (
    <div className="create-store-container">
      <h2>Add New Product</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="create-store-form">
        <div className="form-group">
          <label htmlFor="productName" className='label'>Product Name</label>
          <input
            id="productName"
            placeholder='Enter product name'
            {...register('productName', { onChange: () => trigger("productName") })}
            className={errors.productName && touchedFields.productName ? 'invalid' : ''}
          />
          {errors.productName && <p className="error-message">{errors.productName.message}</p>}
        </div>
        <div className="form-group">
          <label htmlFor="productPrice" className='label'>Product Price</label>
          <input
            id="productPrice"
            placeholder='Enter product price'
            {...register('productPrice', { onChange: () => trigger("productPrice") })}
            className={errors.productPrice && touchedFields.productPrice ? 'invalid' : ''}
          />
          {errors.productPrice && <p className="error-message">{errors.productPrice.message}</p>}
        </div>
        <div className="form-group">
          <label htmlFor="category" className='label'>Product Category</label>
          <input
            id="category"
            placeholder='Enter product category'
            {...register('category', { onChange: () => trigger("category") })}
            list="categs"
            className={errors.category && touchedFields.category ? 'invalid' : ''}
          />
          <datalist id="categs">
          {Categories.map(catego => <option>{catego}</option>)}
          </datalist>
          {errors.category && <p className="error-message">{errors.category.message}</p>}
        </div>
        <div className="form-group">
          <label htmlFor="description" className='label'>Description</label>
          <input
            id="description"
            placeholder='Enter product description'
            {...register('description', { onChange: () => trigger("description") })}
            className={errors.description && touchedFields.description ? 'invalid' : ''}
          />
        </div>
        <div className="form-group">
          <label htmlFor="productWeight" className='label'>Product Weight</label>
          <input
            id="productWeight"
            placeholder='Enter product weight'
            {...register('productWeight', { onChange: () => trigger("productWeight") })}
            className={errors.productWeight && touchedFields.productWeight ? 'invalid' : ''}
          />
          {errors.productWeight && <p className="error-message">{errors.productWeight.message}</p>}
        </div>
        <div className="form-group">
          <label htmlFor="productQuantity" className='label'>Initial Amount</label>
          <input
            id="productQuantity"
            placeholder='Enter initial amount'
            {...register('productQuantity', { onChange: () => trigger("productQuantity") })}
            className={errors.productQuantity && touchedFields.productQuantity ? 'invalid' : ''}
          />
          {errors.productQuantity && <p className="error-message">{errors.productQuantity.message}</p>}
        </div>
        <button type="submit" className='button'>Add Product</button>
        {loading && <Loading reason={"the product is being created"}/>}
      </form>
    </div>
  );
};

export default AddProduct;
