import React, { Component } from 'react';
import { Route, Switch } from 'react-router-dom';

import Header from '../Header';
import Home from '../Home';
import Block from '../Block';
import Transaction from '../Transaction';
import TransactionSend from '../TransactionSend';
import TransactionsView from '../TransactionsView';
import BlockchainView from '../BlockchainVIew';

import './App.css';

class App extends Component {
    constructor(props) {
      super(props);
    }
  
    render() {
      return (
        <div className="App">
          <Header />
          <div id="main">
            <Switch>
              <Route path="/" exact={true} component={Home} />
              <Route path='/block/:hash' component={Block} />
              <Route path='/tx/send' exact={true} component={TransactionSend} />
              <Route path='/tx/view' exact={true} component={TransactionsView} />
              <Route path='/blockchain' exact={true} component={BlockchainView} />
              <Route path='/tx/:txid' component={Transaction} />
            </Switch>
          </div>
        </div>
      );
    }
  }
  

export default App;