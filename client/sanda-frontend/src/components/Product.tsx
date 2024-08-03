import React, { useContext, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import RestResponse from '../models/RestResponse';
import { addProductToCartGuest, addProductToCartMember, getProductAmount, getProductDetails, hasPermission, removeProductFromStore } from '../API';
import ProductModel from '../models/ProductModel';
import { TextField, Button, IconButton, Grid, Box, Typography, Container, Paper } from '@mui/material';
import { AppContext } from '../App';
import AddIcon from '@mui/icons-material/Add';
import RemoveIcon from '@mui/icons-material/Remove';
import Permission from '../models/Permission';

export const Product = () => {
    const { productId } = useParams();
    const [product, setProduct] = useState<ProductModel>({ productID: 0, productPrice: 0, productName: "LOADING", productWeight: 0, productCategory: "LOADING", description: "LOADING"});
    const [amount, setAmount] = useState(1);
    const navigate = useNavigate();
    const { isloggedin, setIsloggedin } = useContext(AppContext);
    const [canDelete, setCanDelete] = useState(false);
    const [canEdit, setCanEdit] = useState(false);
    const [productAmount, setProductAmount] = useState(0);


    useEffect(() => {
        const loadProduct = async () => {
            var productResponse: RestResponse = await getProductDetails(parseInt(productId!));
            if (!productResponse.error) {
                let productData: ProductModel = JSON.parse(productResponse.dataJson);
                setProduct(productData);
                let productStockAmount = await getProductAmount(productData.productID,productData.storeId!);
                setProductAmount(productStockAmount)
                let checkCanDelete: boolean = await hasPermission(`${productData.storeId!}`, Permission.DELETE_PRODUCTS);
                let checkCanUpdate: boolean = await hasPermission(`${productData.storeId!}`, Permission.UPDATE_PRODUCTS);
                
                setCanDelete(checkCanDelete);
                setCanEdit(checkCanUpdate);
            } else {
                navigate('/permission-error', { state: productResponse.errorString });
            }
        };
        loadProduct();
    }, []);


    const addToCart = async () => {
        let response: RestResponse;
        if (isloggedin) {
            response = await addProductToCartMember(parseInt(productId!), product.storeId!, amount);
        } else {
            response = await addProductToCartGuest(parseInt(productId!), product.storeId!, amount);
        }
        if (response.error) {
            alert(`Add to cart failed: ${response.errorString}`);
        } else {
            alert("Product added to cart!");
        }
    };
    
    const removeFromStore = async () => {
        let response: RestResponse;
        response = await removeProductFromStore(parseInt(productId!), product.storeId!);
        if (response.error) {
            alert(`Remove from store failed: ${response.errorString}`);
        } else {
            alert("Product removed from store!");
            navigate('/');
        }
    };

    const handleAmountChange = (operation: 'increase' | 'decrease') => {
        setAmount((prevAmount) => {
            if (operation === 'increase') {
                return prevAmount + 1;
            } else if (operation === 'decrease' && prevAmount > 1) {
                return prevAmount - 1;
            }
            return prevAmount;
        });
    };

    return (
        <Container component="main" maxWidth="sm">
            <Paper elevation={3} sx={{ padding: 3, marginTop: 4 }}>
                <Typography variant="h4" component="h1" gutterBottom>
                    Product Details
                </Typography>
                <Typography variant="h5" component="h2" gutterBottom>
                    {product.productName}
                </Typography>
                <Typography variant="h6" component="h3" gutterBottom>
                    Price: ${product.productPrice}
                </Typography>
                <Typography variant="body1" gutterBottom>
                    Weight: {product.productWeight ? product.productWeight : 'N/A'} g
                </Typography>
                <Typography variant="body1" gutterBottom>
                    Category: {product.productCategory}
                </Typography>
                <Typography variant="body1" gutterBottom>
                    Description: {product.description ? product.description : 'N/A'}
                </Typography>
                <Typography variant="body1" gutterBottom>
                    Rank: {product.productRank ? product.productRank : 'N/A'}
                </Typography>
                <Typography variant="body1" gutterBottom>
                    Current Amount: {productAmount ? productAmount : 'N/A'}
                </Typography>
                <Grid container spacing={2} alignItems="center">
                    <Grid item>
                        <IconButton color="primary" onClick={() => handleAmountChange('decrease')}>
                            <RemoveIcon />
                        </IconButton>
                    </Grid>
                    <Grid item>
                        <TextField
                            type='number'
                            size='small'
                            variant="outlined"
                            value={amount}
                            onChange={(event: React.ChangeEvent<HTMLInputElement>) => { setAmount(parseInt(event.target.value)); }}
                            inputProps={{ min: 1 }}
                        />
                    </Grid>
                    <Grid item>
                        <IconButton color="primary" onClick={() => handleAmountChange('increase')}>
                            <AddIcon />
                        </IconButton>
                    </Grid>
                </Grid>
                <Box mt={2}>
                    {canDelete && <Button variant="contained" color="error" onClick={removeFromStore} fullWidth>
                        Remove From Store
                    </Button>}
                    {canEdit && <Button variant="contained" color="primary" onClick={() => navigate(`/editproduct/${product.productID}`)} fullWidth>
                        Update Product
                    </Button>}
                </Box>
                <Box mt={2}>
                    <Button variant="contained" color="primary" onClick={addToCart} fullWidth>
                        Add to Cart
                    </Button>
                </Box>
                <Box mt={2}>
                    <Button variant="outlined" color="secondary" onClick={() => navigate(`/store/${product.storeId}`)} fullWidth>
                        view Store
                    </Button>
                </Box>
            </Paper>
        </Container>
    );
};
