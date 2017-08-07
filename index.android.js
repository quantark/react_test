/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  TextInput,
  View,
  Button,
  FlatList,
  List,
  SearchBar,
  BackHandler
} from 'react-native';

import { NativeModules } from 'react-native'; 

export default class TestProject1 extends Component {
	
	constructor(props){
		super(props);
		this.state = {
			data: ["Item1", "Item2"],
			uri: "root"
		};
	}
	
	updateData(newData){
		this.setState({ data: newData});
	}
	
	_getParent(uri){
		var parts = uri.split("\/");
		parts.splice(-2, 2);
		return parts.join("\/");
	}
	
	_getFileName(uri){
		var parts = uri.split("\/");
		return parts.pop();
	}
	
	componentDidMount(){
		
		BackHandler.addEventListener('hardwareBackPress', function() {  
			this._goToParent(this.state.uri)
			return true; 
		});
		
		this._getRootFile();
	}
	
	_getRootFile(){
		NativeModules.MyToastAndroid.getFilesList("", (list) => {
			this.updateData(list);
		})
	}
	
	
	_goToParent(uri){
		this._goToFile(this._getParent(uri));
	}
	
	_goToFile(newUri){
		NativeModules.MyToastAndroid.getFilesList(newUri, (list) => {
			if (typeof list !== 'undefined' && list.length > 0) {
				this.updateData(list);
				this.state.uri = newUri;
			} else {
				//NativeModules.MyToastAndroid.show("No items found", 0);
			}
		})
	}
	
//	renderHeader = () => {
//		return <SearchBar placeholder="Type Here..." lightTheme round />;
//		return <Text 
//			onPress={(v) => 
//					this._goToParent(this.state.uri)}
//		>"..."</Text>;
//	};
	
	renderHeader = () => {
		return <SearchBar placeholder="Type Here..." lightTheme round />;
	};
	
	
	_keyExtractor = (item, index) => item;
	
  render() {
    return (
		<FlatList
			data={this.state.data}
			renderItem={({item}) => <Text 
				style={styles.item}
				onPress={(id) => {
					this._goToFile(item);
					//NativeModules.MyToastAndroid.show(this._getParent(item).toString(), 0);
				}}
			>{item}</Text>}
		/>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
  item: {
	  padding: 10,
	  fontSize: 18,
	  height: 80,
  },
});

AppRegistry.registerComponent('TestProject1', () => TestProject1);
